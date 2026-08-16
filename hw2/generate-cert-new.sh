#!/bin/bash

# Настройки
PASSWORD=Password1_
VALIDITY=365
CA_ALIAS=CARoot
ROOT_DIR=./ssl

# Очистка старых сертификатов
rm -rf $ROOT_DIR
mkdir -p $ROOT_DIR

# Создание CA (Certificate Authority)
openssl req -new -x509 \
  -keyout $ROOT_DIR/ca-key \
  -out $ROOT_DIR/ca-cert \
  -days $VALIDITY \
  -subj "/CN=Kafka-CA" \
  -passout pass:$PASSWORD

# Конвертация CA в JKS truststore
keytool -keystore $ROOT_DIR/ca-truststore.jks \
  -alias $CA_ALIAS \
  -import -file $ROOT_DIR/ca-cert \
  -storepass $PASSWORD \
  -keypass $PASSWORD \
  -noprompt

# Функция для создания keystore для каждого брокера
create_keystore() {
  BROKER=$1
  BROKER_HOST=$1

  mkdir -p $ROOT_DIR/$BROKER

  # Генерация ключа и сертификата для брокера
  keytool \
	-genkey \
	-keyalg RSA \
    -keystore $ROOT_DIR/$BROKER/$BROKER.keystore.jks \
    -alias $BROKER \
    -validity $VALIDITY \
    -storetype pkcs12 \
	-storepass $PASSWORD \
	-keypass $PASSWORD \
    -dname "CN=$BROKER_HOST,OU=Kafka,O=Otus,L=Moscow,ST=Moscow,C=RU" 

  # Создание запроса на подпись сертификата
  keytool -keystore $ROOT_DIR/$BROKER/$BROKER.keystore.jks \
    -alias $BROKER \
    -certreq -file $ROOT_DIR/$BROKER/$BROKER.csr \
    -storepass $PASSWORD \
    -keypass $PASSWORD

  # СОЗДАЕМ ФАЙЛ КОНФИГУРАЦИИ ДЛЯ SAN
  cat > $ROOT_DIR/$BROKER/san.cnf <<EOF
[v3_req]
subjectAltName=DNS:$BROKER_HOST,DNS:localhost,IP:127.0.0.1
EOF

  # Подписание сертификата с помощью CA с добавлением SAN
  openssl x509 -req -CA $ROOT_DIR/ca-cert -CAkey $ROOT_DIR/ca-key \
    -in $ROOT_DIR/$BROKER/$BROKER.csr -out $ROOT_DIR/$BROKER/$BROKER-cert-signed \
    -days $VALIDITY \
    -CAcreateserial \
    -passin pass:$PASSWORD \
    -extensions v3_req \
    -extfile $ROOT_DIR/$BROKER/san.cnf

  # Импорт CA сертификата в keystore
  keytool -keystore $ROOT_DIR/$BROKER/$BROKER.keystore.jks \
    -alias $CA_ALIAS \
    -import -file $ROOT_DIR/ca-cert \
    -storepass $PASSWORD \
    -keypass $PASSWORD \
    -noprompt

  # Импорт подписанного сертификата в keystore
  keytool -keystore $ROOT_DIR/$BROKER/$BROKER.keystore.jks \
    -alias $BROKER \
    -import -file $ROOT_DIR/$BROKER/$BROKER-cert-signed \
    -storepass $PASSWORD \
    -keypass $PASSWORD \
    -noprompt

  # Создание truststore для брокера
  keytool -keystore $ROOT_DIR/$BROKER/$BROKER.truststore.jks \
    -alias $CA_ALIAS \
    -import -file $ROOT_DIR/ca-cert \
    -storepass $PASSWORD \
    -keypass $PASSWORD \
    -noprompt

  # Очистка временных файлов
  rm -f $ROOT_DIR/$BROKER/$BROKER.csr $ROOT_DIR/$BROKER/$BROKER-cert-signed $ROOT_DIR/$BROKER/san.cnf

  cp $ROOT_DIR/ca-cert $ROOT_DIR/$BROKER/ca-cert
  cp $ROOT_DIR/ca-truststore.jks $ROOT_DIR/$BROKER/ca-truststore.jks

  echo "Созданы сертификаты для $BROKER"
}

# Функция для создания keystore для клиента
create_for_client() {
  CLIENT_NAME=$1

  mkdir -p $ROOT_DIR/$CLIENT_NAME

  # Генерация клиентского keystore
  keytool -keystore $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME.keystore.jks \
    -alias $CLIENT_NAME \
    -validity $VALIDITY \
    -genkey -keyalg RSA \
    -dname "CN=$CLIENT_NAME" \
    -storepass $PASSWORD \
    -keypass $PASSWORD

  # Запрос на подпись
  keytool -keystore $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME.keystore.jks \
    -alias $CLIENT_NAME \
    -certreq -file $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME.csr \
    -storepass $PASSWORD \
    -keypass $PASSWORD

  # Подписание CA
  openssl x509 -req -CA $ROOT_DIR/ca-cert -CAkey $ROOT_DIR/ca-key \
    -in $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME.csr -out $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME-cert-signed \
    -days $VALIDITY \
    -CAcreateserial \
    -passin pass:$PASSWORD

  # Импорт CA в клиентский keystore
  keytool -keystore $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME.keystore.jks \
    -alias $CA_ALIAS \
    -import -file $ROOT_DIR/ca-cert \
    -storepass $PASSWORD \
    -keypass $PASSWORD \
    -noprompt

  # Импорт подписанного сертификата
  keytool -keystore $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME.keystore.jks \
    -alias $CLIENT_NAME \
    -import -file $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME-cert-signed \
    -storepass $PASSWORD \
    -keypass $PASSWORD \
    -noprompt

  # Создание truststore для клиента
  keytool -keystore $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME.truststore.jks \
    -alias $CA_ALIAS \
    -import -file $ROOT_DIR/ca-cert \
    -storepass $PASSWORD \
    -keypass $PASSWORD \
    -noprompt

  # Очистка
  rm -f $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME.csr $ROOT_DIR/$CLIENT_NAME/$CLIENT_NAME-cert-signed

  echo "Созданы сертификаты для клиента $CLIENT_NAME"
}

# Создание keystore для каждого брокера
create_keystore "kafka1"
create_keystore "kafka2"
create_keystore "kafka3"

# Создание клиентских сертификатов
create_for_client "client"
create_for_client "client1"
create_for_client "client2"

echo " SSL сертификаты созданы успешно!"
