/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.gateway.wunderground;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.McThreadPoolFactory;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.GatewayAbstract;
import org.mycontroller.standalone.gateway.config.GatewayConfigWunderground;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.offheap.IQueue;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public class GatewayWunderground extends GatewayAbstract {
    private GatewayConfigWunderground _config;
    private WundergroundDriver _driver;
    private IQueue<IMessage> _queue = null;

    public GatewayWunderground(GatewayTable gatewayTable, IQueue<IMessage> queue) {
        super(new GatewayConfigWunderground(gatewayTable));
        _config = (GatewayConfigWunderground) config();
        _driver = new WundergroundDriver(_config, queue);
        this._queue = queue;
    }

    @Override
    public void write(IMessage message) throws MessageParserException {
        _driver.write(message);
    }

    @Override
    public void connect() {
        _driver.connect();
        McThreadPoolFactory.execute(_driver);
    }

    @Override
    public void disconnect() {
        _driver.disconnect();
    }

    @Override
    public void reconnect() {
        disconnect();
        // TODO: add option to wait until terminate
        _driver = new WundergroundDriver(_config, _queue);
        connect();
    }

    @Override
    public boolean isUp() {
        return _config.getState() == STATE.UP;
    }

}
