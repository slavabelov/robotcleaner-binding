/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.robotcleaner.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RobotCleanerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Slava Belov - Initial contribution
 */
@NonNullByDefault
public class RobotCleanerBindingConstants {

    private static final String BINDING_ID = "robotcleaner";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "robotcleaner");

    // List of all Channel ids
    public static final String CHANNEL_ROBOT_CAN_CLEAN = "can-clean";
    public static final String CHANNEL_ROBOT_STATUS = "status";
    public static final String CHANNEL_ROBOT_IS_CHARGING = "is-charging";
    public static final String CHANNEL_ROBOT_BATTERY = "battery";
    public static final String CHANNEL_ROBOT_ERROR = "error";
}
