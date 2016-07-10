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

package org.autobet.ioc;

import dagger.Module;
import dagger.Provides;
import org.flywaydb.core.Flyway;
import org.javalite.activejdbc.Base;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Module
public class DatabaseConnectionModule
{
    @Singleton
    @Provides
    @Inject
    public DatabaseConnection provideConnection(DataSource dataSource)
    {
        return new DatabaseConnection(dataSource);
    }

    public static class DatabaseConnection
            implements AutoCloseable
    {
        public DatabaseConnection(DataSource dataSource)
        {
            Flyway flyway = new Flyway();
            flyway.setDataSource(dataSource);
            flyway.migrate();
            Base.open(dataSource);
        }

        @Override
        public void close()
        {
            Base.close();
        }
    }
}
