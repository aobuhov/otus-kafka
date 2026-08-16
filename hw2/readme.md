1. генерируем свой CLUESTER_ID для будущего кластера серверов Kafka
```python
python -c "import uuid, base64; print(base64.urlsafe_b64encode(uuid.uuid4().bytes)decode().rstrip('='))"
```
![[01-gen-cluster-guid.png]]
2. подставляем сгенерированный guid в docker-compose файл, чтобы у нашего кластера был свой собственый
![[02-set-cluster-id-to-docker-compose.png]]
3.  запускаем docker-compose.yml 
![[03-stark-docker-compose.png]]
4.  проверяем, что наш  GUID подставился
![[04-check-clusted-id.png]]
5. подготоваливаем скринт для генерации SSL-сертификатов. Скрипт генерирует следующую структура файлов
				ssl/
				├── ca/
				│   ├── ca.key
				│   ├── ca.crt
				│   └── ca-truststore.jks
				├── brokers/
				│   ├── kafka1/
				│   │   ├── kafka1.keystore.jks
				│   │   └── kafka1.truststore.jks
				│   ├── kafka2/
				│   │   ├── kafka2.keystore.jks
				│   │   └── kafka2.truststore.jks
				│   └── kafka3/
				│       ├── kafka3.keystore.jks
				│       └── kafka3.truststore.jks
				└── clients/
				    ├── client1/
				    │   ├── client1.keystore.jks
				    │   └── client1.truststore.jks
				    ├── client2/
				    │   ├── client2.keystore.jks
				    │   └── client2.truststore.jks
				    └── client3/
				        ├── client3.keystore.jks
				        └── client3.truststore.jks
6. подготавливаем обновлённый docker compose 
7. запускаем кластер
8. проверяем что всё работает
9. подготавливаем файл client-ssl.properties
10. пробуем подключиться и выполнить следующие действия
11. просматриваем список топиков
./kafka-topics.sh --list --bootstrap-server localhost:9092 --command-config client-ssl.properties
12. создаём топик
./kafka-topics.sh --create --topic test --bootstrap-server localhost:9093 --command-config client-ssl.properties
13. отправляем сообщения в топик
./kafka-console-producer.sh --bootstrap-server localhost:9093 --topic test --producer.config client-ssl.properties
14. читаем сообщения из топика
./kafka-console-consumer.sh --bootstrap-server localhost:9093 --topic test --consumer.config client-ssl.properties -from-beginning
15. проверяем конфигурацию брокера
./kafka-configs.sh --bootstrap-server localhost:9092 --entity-type brokers --entity-name 1 --describe --all --command-config client-ssl.properties | grep -i ssl | sort
16.  Подключение по SSL с аутентификацией
через настройки через внешний файл у меня таки и не получилось пропихнуть новые настройки, поэтому менял в самом docker  compose файле
17. Создаём client-ssl-auth.properties
security.protocol=SSL
ssl.keystore.location=/home/obukhov/projects/OTUS-KAFKA/hw-impl/hw2/ssl/client1/client1.keystore.jks
ssl.keystore.password=Password1_
ssl.key.password=Password1_
ssl.truststore.location=/home/obukhov/projects/OTUS-KAFKA/hw-impl/hw2/ssl/client1/client1.truststore.jks
ssl.truststore.password=Password1_
ssl.endpoint.identification.algorithm=
max.block.ms=60000
request.timeout.ms=60000
delivery.timeout.ms=120000
socket.connection.setup.timeout.ms=60000
socket.connection.setup.timeout.max.ms=120000
connections.max.idle.ms=600000
18. устанавливаем required  в docker compose файле
19. пытаемся подключиться с использованием client-ssl.properties
./kafka-topics.sh --list --bootstrap-server localhost:9092 --command-config client-ssl.properties
20. получаем ошибку 
21. подключаемся в client-ssl-auth.properties
./kafka-topics.sh --list --bootstrap-server localhost:9092 --command-config client-ssl-auth.properties
22. создаём топик, публикум несколько сообщений, читаем их
./kafka-topics.sh --create --topic test --bootstrap-server localhost:9093 --command-config client-ssl-auth.propertieses
./kafka-console-producer.sh --bootstrap-server localhost:9093 --topic test --producer.config client-ssl-auth.properties
./kafka-console-consumer.sh --bootstrap-server localhost:9093 --topic test --consumer.config client-ssl-auth.properties -from-beginning
23. проверяем конфиг
./kafka-configs.sh --bootstrap-server localhost:9093 --entity-type brokers --entity-name 2 --describe --all --command-config client-ssl-auth.properties | grep -i ssl | sort
24. создаём файл kafka_server_jaas.conf
25. создаём новый docker-compose.kraft.sasl.yml
26. запускаем кластер 
27. создаём настройки для клиентов: 
		client-client-plain.properties
		client-client-ssl.properties
		client-client1-plain.properties
		client-client1-ssl.properties
		client-client2-plain.properties
		client-client2-ssl.properties
28. подключаемся клиентом
./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic test --partitions 3 --replication-factor 3 --command-config client-sasl-ssl.properties
./kafka-topics.sh --list --bootstrap-server localhost:9092 --command-config client-sasl-ssl.properties
./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test --producer.config client-sasl-ssl.properties
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --consumer.config client-sasl-ssl.properties --from-beginning
29. Проверка SASL_PLAINTEXT
kafka-topics.sh --list --bootstrap-server localhost:9095 --command-config client-sasl-plain.properties
30. Создание ACL правил
./kafka-acls.sh --bootstrap-server localhost:9095 --list --command-config client-sasl-plain.properties
./kafka-acls.sh --bootstrap-server localhost:9095 --add --allow-principal User:Alice --operation Write --topic test --command-config client-sasl-plain.properties
./kafka-acls.sh --bootstrap-server localhost:9095 --add --allow-principal User:Bob --operation Read --topic test --command-config client-sasl-plain.properties
./kafka-acls.sh --bootstrap-server localhost:9095 --add --allow-principal User:A2 --operation Write --operation Read --operation Describe --topic test --command-config client-sasl-plain.properties
31. проверяем права
./kafka-console-consumer.sh --bootstrap-server localhost:9095 --topic test --consumer.config client-client2-plain.properties --from-beginning
./kafka-console-producer.sh --bootstrap-server localhost:9095 --topic test --producer.config client-client1-plain.properties
./kafka-console-producer.sh --bootstrap-server localhost:9095 --topic test --producer.config client-client-plain.properties

