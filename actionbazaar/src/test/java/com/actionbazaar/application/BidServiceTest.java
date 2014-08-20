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
package com.actionbazaar.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.actionbazaar.domain.Bid;
import com.actionbazaar.domain.BidRepository;
import com.actionbazaar.infrastructure.database.DefaultBidRepository;

@RunWith(Arquillian.class)
public class BidServiceTest {

    private static Long bidId;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class, "actionbazaar-test.war")
                .addClasses(BidService.class, DefaultBidService.class,
                        Profiled.class, ProfilingInterceptor.class,
                        BidRepository.class, DefaultBidRepository.class, Bid.class)
                .addAsWebInfResource("test-beans.xml", "beans.xml")
                .addAsResource("test-persistence.xml",
                        "META-INF/persistence.xml");
    }

    @EJB
    private BidService bidService;

    @Test
    @InSequence(1)
    public void testAddBid() {
        // Save a new bid.
        Bid bid = new Bid();
        bid.setBidder("rrahman");
        bid.setItem("Test item");
        bid.setAmount(100.50);

        bidService.addBid(bid);

        // Make sure it was correctly saved.
        bidId = bid.getId();

        bid = bidService.getBid(bidId);

        assertEquals("rrahman", bid.getBidder());
        assertEquals("Test item", bid.getItem());
        assertEquals(new Double(100.50), bid.getAmount());
    }

    @Test
    @InSequence(2)
    public void testUpdateBid() {
        // Update bid.
        Bid bid = bidService.getBid(bidId);
        bid.setAmount(101.50);
        bidService.updateBid(bid);

        // Make sure bid was updated.
        bid = bidService.getBid(bidId);

        assertEquals("rrahman", bid.getBidder());
        assertEquals("Test item", bid.getItem());
        assertEquals(new Double(101.50), bid.getAmount());
    }

    @Test
    @InSequence(3)
    public void testDeleteBid() {
        Bid bid = bidService.getBid(bidId);
        bidService.deleteBid(bid);

        assertNull(bidService.getBid(bidId));
    }
}
