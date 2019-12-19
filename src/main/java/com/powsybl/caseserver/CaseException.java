/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.caseserver;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class CaseException extends RuntimeException {

    public CaseException() {
    }

    public CaseException(String msg) {
        super(msg);
    }

    public CaseException(Throwable throwable) {
        super(throwable);
    }

    public CaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
