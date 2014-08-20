/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.actionbazaar.interfaces.socket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ChatServerTest {

    private static final Logger logger
            = Logger.getLogger(ChatServerTest.class.getName());

    private static ChatMessage testMessage;
    private static ChatMessage testReply;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class, "actionbazaar-test.war")
                .addClass(ChatMessage.class)
                .addClass(ChatServer.class);
    }

    @Test
    public void testChat() {
        try {
            URI uri = new URI("ws://localhost:7001/actionbazaar-test/chat");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            ClientEndpointConfig configuration = ClientEndpointConfig.Builder.create()
                    .decoders(Arrays.<Class<? extends Decoder>>asList(ChatMessage.class))
                    .encoders(Arrays.<Class<? extends Encoder>>asList(ChatMessage.class))
                    .build();

            Endpoint client1 = new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    try {
                        session.addMessageHandler(new MessageHandler.Whole<ChatMessage>() {
                            @Override
                            public void onMessage(ChatMessage message) {
                                testReply = message;
                            }
                        });
                        session.getBasicRemote().sendObject(
                                new ChatMessage("rrahman", "Test message"));
                    } catch (IOException | EncodeException e) {
                        logger.log(Level.SEVERE, "Error in chat client", e);
                    }
                }
            };

            Endpoint client2 = new Endpoint() {
                @Override
                public void onOpen(final Session session, final EndpointConfig config) {
                    session.addMessageHandler(new MessageHandler.Whole<ChatMessage>() {
                        @Override
                        public void onMessage(ChatMessage message) {
                            try {
                                testMessage = message;
                                session.getBasicRemote().sendObject(new ChatMessage("nrahman", "Test reply"));
                            } catch (IOException | EncodeException ex) {
                                logger.log(Level.SEVERE, "Error responding to message", ex);
                            }
                        }
                    });
                }
            };

            container.connectToServer(client2, configuration, uri);
            container.connectToServer(client1, configuration, uri);

            // Wait for conversation to finish.
            Thread.sleep(2000);

            assertEquals("rrahman", testMessage.getUser());
            assertEquals("Test message", testMessage.getMessage());
            assertEquals("nrahman", testReply.getUser());
            assertEquals("Test reply", testReply.getMessage());
        } catch (URISyntaxException | DeploymentException | IOException | InterruptedException ex) {
            logger.log(Level.SEVERE, "Error connecting to server", ex);
        }
    }
}
