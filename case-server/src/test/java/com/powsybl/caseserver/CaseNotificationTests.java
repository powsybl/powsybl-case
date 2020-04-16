/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.caseserver;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesMessageThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.integration.test.matcher.PayloadAndHeaderMatcher.sameExceptIgnorableHeaders;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {CaseController.class, ContextFunctionCatalogAutoConfiguration.class})
@DirtiesContext
class CaseNotificationTests {

    @Autowired
    @Qualifier("publishCaseImport-out-0")
    private MessageChannel output;

    @Autowired
    private MessageCollector collector;

    @Test
    @SuppressWarnings("unchecked")
    void testMessages() {
        CaseInfos caseInfos = new CaseInfos("testCase.xml", "CGMES", UUID.randomUUID());

        this.output.send(CaseInfos.getMessage(caseInfos));

        BlockingQueue<Message<?>> messages = this.collector.forChannel(this.output);

        assertThat(messages, receivesPayloadThat(CoreMatchers.is("")));

        Message<String> message = CaseInfos.getMessage(caseInfos);
        this.output.send(message);

        Matcher<Message<Object>> sameExceptIgnorableHeaders =  (Matcher<Message<Object>>) (Matcher<?>) sameExceptIgnorableHeaders(message);
        assertThat(messages, receivesMessageThat(sameExceptIgnorableHeaders));
    }

}
