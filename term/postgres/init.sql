-- init.sql
-- Выполняется от имени суперпользователя postgres в БД db

-- 1. Пользователи
CREATE USER orders_user      WITH PASSWORD 'AppAdmin123!';
CREATE USER restaurants_user WITH PASSWORD 'AppAdmin123!';
CREATE USER payments_user    WITH PASSWORD 'AppAdmin123!';
CREATE USER delivery_user    WITH PASSWORD 'AppAdmin123!';

-- 2. Базы данных с owner = соответствующий пользователь
CREATE DATABASE orders_db      OWNER orders_user;
CREATE DATABASE restaurants_db OWNER restaurants_user;
CREATE DATABASE payments_db    OWNER payments_user;
CREATE DATABASE delivery_db    OWNER delivery_user;

-- 3. На всякий случай запретить public-доступ
\connect orders_db
GRANT ALL ON SCHEMA public TO orders_user;
ALTER SCHEMA public OWNER TO orders_user;

\connect restaurants_db
GRANT ALL ON SCHEMA public TO restaurants_user;
ALTER SCHEMA public OWNER TO restaurants_user;

\connect payments_db
GRANT ALL ON SCHEMA public TO payments_user;
ALTER SCHEMA public OWNER TO payments_user;

\connect delivery_db
GRANT ALL ON SCHEMA public TO delivery_user;
ALTER SCHEMA public OWNER TO delivery_user;