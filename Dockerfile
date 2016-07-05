FROM postgres

COPY src/main/resources/schema.sql /docker-entrypoint-initdb.d/


