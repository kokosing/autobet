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

import org.autobet.ioc.DaggerMainComponent;
import org.autobet.ioc.DataSourceModule;
import org.autobet.ioc.DatabaseConnectionModule;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.io.File;
import java.io.IOException;

public class TemporaryDatabase
        extends ExternalResource
{
    private static final Logger log = LoggerFactory.getLogger(TemporaryDatabase.class);

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private final boolean load;
    private DatabaseConnectionModule.DatabaseConnection connection;

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
    protected void before()
            throws Throwable
    {
        super.before();
        temporaryFolder.create();
        connection = DaggerMainComponent.builder().dataSourceModule(new DataSourceModule()
        {
            @Override
            public DataSource provideDataSource()
            {
                JdbcDataSource dataSource = new JdbcDataSource();
                dataSource.setURL("jdbc:h2:" + newFile().getAbsolutePath());
                dataSource.setUser("sa");
                dataSource.setPassword("sa");
                return dataSource;
            }
        }).build().connectToDatabase();

        if (load) {
            Loader loader = new Loader();
            loader.load("data/www.football-data.co.uk/mmz4281/0001/B1.csv");
            log.info("loaded");
        }
    }

    private File newFile()
    {
        try {
            return temporaryFolder.newFile();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void after()
    {
        connection.close();
        temporaryFolder.delete();
        super.after();
    }
}
