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

import static org.openhab.binding.robotcleaner.internal.RobotCleanerBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RobotCleanerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Slava Belov - Initial contribution
 */
@NonNullByDefault
public class RobotCleanerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RobotCleanerHandler.class);

    private @Nullable RobotCleanerConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private int batteryLevel = 100;

    private boolean canClean = false;

    private boolean isCleaning = false;

    public RobotCleanerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshChannels(channelUID);
        } else {
            sendCommand(channelUID, command);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private void sendCommand(ChannelUID channelUID, Command command) {
        logger.info("sendCommand {} {}", channelUID.getId(), command.toString());
        switch (channelUID.getId()) {
            case CHANNEL_ROBOT_CAN_CLEAN:
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON && canClean == false) {
                        canClean = true;
                        isCleaning = true;
                        logger.info("Robot can clean now");
                    } else if (command == OnOffType.OFF && canClean == true) {
                        canClean = false;
                        isCleaning = false;
                        logger.info("Robot can not clean now");
                    }
                } else {
                    logger.warn("Got stopped update of type {} but OnOffType is expected.",
                            command.getClass().getName());
                }
                break;
        }
    }

    private void refreshChannels(ChannelUID channelUID) {
        logger.info("refreshChannels {}", channelUID.getId());
        refreshCleanerInfo();
    }

    private void refreshCleanerInfo() {
        logger.info("Refreshing robot cleaner info");
        if (canClean) {
            updateState(CHANNEL_ROBOT_CAN_CLEAN, OnOffType.ON);
        } else {
            updateState(CHANNEL_ROBOT_CAN_CLEAN, OnOffType.OFF);
        }
        if (isCleaning) {
            updateState(CHANNEL_ROBOT_STATUS, OnOffType.ON);
            updateState(CHANNEL_ROBOT_IS_CHARGING, OnOffType.OFF);
        } else {
            updateState(CHANNEL_ROBOT_STATUS, OnOffType.OFF);
            updateState(CHANNEL_ROBOT_IS_CHARGING, OnOffType.ON);
        }
        updateState(CHANNEL_ROBOT_BATTERY, new DecimalType(batteryLevel));
        updateState(CHANNEL_ROBOT_ERROR, new StringType(new String("No error")));
        if (isCleaning) {
            batteryLevel = batteryLevel - 1;
            if (batteryLevel < 0) {
                batteryLevel = 0;
            }
        } else {
            batteryLevel = batteryLevel + 1;
            if (batteryLevel > 100) {
                batteryLevel = 100;
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(RobotCleanerConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        Runnable runnable = new RobotCleanerChannelPoller();
        int pollInterval = config.getRefreshInterval();
        pollingJob = scheduler.scheduleWithFixedDelay(runnable, 0, pollInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private class RobotCleanerChannelPoller implements Runnable {
        public RobotCleanerChannelPoller() {
        }

        @Override
        public void run() {
            refreshCleanerInfo();
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
