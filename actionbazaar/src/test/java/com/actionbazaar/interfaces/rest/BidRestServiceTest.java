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
package com.actionbazaar.interfaces.rest;

import com.actionbazaar.application.BidService;
import com.actionbazaar.application.DefaultBidService;
import com.actionbazaar.domain.Bid;
import com.actionbazaar.domain.BidRepository;
import com.actionbazaar.infrastructure.database.DefaultBidRepository;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
// TODO Move this to client side.
public class BidRestServiceTest {

    private static Long bidId;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class, "actionbazaar-test.war")
                .addClasses(BidRestService.class, RestConfiguration.class,
                        BidService.class, DefaultBidService.class,
                        BidRepository.class, DefaultBidRepository.class, Bid.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("test-weblogic.xml", "weblogic.xml")
                .addAsResource("test-persistence.xml",
                        "META-INF/persistence.xml");
    }

    @Test
    @InSequence(1)
    public void testAddBid() {
        WebTarget target = ClientBuilder.newClient()
                .target("http://localhost:7001/actionbazaar-test/rest/bids");
        // Save a new bid.
        Bid bid = new Bid();

        bid.setBidder("nrahman");
        bid.setItem("Test item");
        bid.setAmount(130.75);

        bid = target.request("application/json").post(Entity.json(bid), Bid.class);

        // Make sure it was correctly saved.
        bidId = bid.getId();

        bid = target.path("{id}").resolveTemplate("id", bidId)
                .request("application/json").get(Bid.class);

        assertEquals("nrahman", bid.getBidder());
        assertEquals("Test item", bid.getItem());
        assertEquals(new Double(130.75), bid.getAmount());
    }

    @Test
    @InSequence(2)
    public void testUpdateBid() {
        WebTarget target = ClientBuilder.newClient()
                .target("http://localhost:7001/actionbazaar-test/rest/bids/{id}")
                .resolveTemplate("id", bidId);

        // Update bid.
        Bid bid = target.request("application/json").get(Bid.class);

        bid.setAmount(150.50);

        target.request().put(Entity.json(bid));

        // Make sure bid was updated.
        bid = target.request("application/json").get(Bid.class);

        assertEquals("nrahman", bid.getBidder());
        assertEquals("Test item", bid.getItem());
        assertEquals(new Double(150.50), bid.getAmount());
    }

    @Test
    @InSequence(3)
    public void testDeleteBid() {
        WebTarget target = ClientBuilder.newClient()
                .target("http://localhost:7001/actionbazaar-test/rest/bids/{id}")
                .resolveTemplate("id", bidId);

        target.request().delete();

        assertNull(target.request("application/json").get(Bid.class));
    }
}
