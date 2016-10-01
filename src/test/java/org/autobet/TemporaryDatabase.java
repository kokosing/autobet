/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.autobet;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.autobet.ioc.AIModule;
import org.autobet.ioc.DaggerMainComponent;
import org.autobet.ioc.DataSourceModule;
import org.autobet.ioc.DatabaseConnectionModule;
import org.autobet.ioc.MainComponent;
import org.autobet.util.GamesProcessorDriver;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;

public class TemporaryDatabase
        extends ExternalResource
{
    private static final Logger log = LoggerFactory.getLogger(TemporaryDatabase.class);

    private final MySQLContainer mySQLContainer = new MySQLContainer("kokosing/mysql:latest");
    private final boolean load;
    private DatabaseConnectionModule.DatabaseConnection connection;
    private MainComponent mainComponent;

    public static TemporaryDatabase loaded()
    {
        return new TemporaryDatabase(true);
    }

    public static TemporaryDatabase empty()
    {
        return new TemporaryDatabase(false);
    }

    private TemporaryDatabase(boolean load)
    {
        this.load = load;
    }

    @Override
    public void before()
            throws Throwable
    {
        super.before();
        mySQLContainer.start();
        mainComponent = DaggerMainComponent.builder()
                .aIModule(new AIModule()
                {
                    @Override
                    public GamesProcessorDriver provideDriver(DataSource dataSource)
                    {
                        return new GamesProcessorDriver(dataSource, 1);
                    }
                })
                .dataSourceModule(new DataSourceModule()
                {
                    @Override
                    public DataSource provideDataSource()
                    {
                        MysqlDataSource dataSource = new MysqlDataSource();
                        dataSource.setURL(mySQLContainer.getJdbcUrl());
                        dataSource.setUser(mySQLContainer.getUsername());
                        dataSource.setPassword(mySQLContainer.getPassword());
                        return dataSource;
                    }
                }).build();
        connection = mainComponent.connectToDatabase();

        if (load) {
            Loader loader = new Loader();
            int loadedCount = loader.load("data/www.football-data.co.uk/mmz4281/0001/B1.csv");
            log.info("loaded objects: " + loadedCount);
        }
    }

    @Override
    public void after()
    {
        connection.close();
        mySQLContainer.stop();
        super.after();
    }

    public MainComponent getComponent()
    {
        return mainComponent;
    }
}
