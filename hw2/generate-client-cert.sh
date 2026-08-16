#!/bin/bash

PASSWORD=Password1_
VALIDITY=365
CA_ALIAS=CARoot
DIR=./ssl

CLIENT_NAME="kafka-client"

# Проверка наличия CA
if [ ! -f "$DIR/ca-cert" ] || [ ! -f "$DIR/ca-key" ]; then
    echo "Ошибка: запустите generate-certs.sh сначала"
    exit 1
fi

echo "Создание сертификата для $CLIENT_NAME..."

# Генерация клиентского keystore
keytool -keystore $DIR/$CLIENT_NAME.keystore.jks \
    -alias $CLIENT_NAME \
    -validity $VALIDITY \
    -genkey -keyalg RSA \
    -dname "CN=$CLIENT_NAME" \
    -storepass $PASSWORD \
    -keypass $PASSWORD

# Запрос на подпись
keytool -keystore $DIR/$CLIENT_NAME.keystore.jks \
    -alias $CLIENT_NAME \
    -certreq -file $CLIENT_NAME.csr \
    -storepass $PASSWORD \
    -keypass $PASSWORD

# Подписание CA
openssl x509 -req -CA ca-cert -CAkey ca-key \
    -in $DIR/$CLIENT_NAME.csr -out $DIR/$CLIENT_NAME-cert-signed \
    -days $VALIDITY \
    -CAcreateserial \
    -passin pass:$PASSWORD

# Импорт CA в клиентский keystore
keytool -keystore $DIR/$CLIENT_NAME.keystore.jks \
    -alias $CA_ALIAS \
    -import -file ca-cert \
    -storepass $PASSWORD \
    -keypass $PASSWORD \
    -noprompt

# Импорт подписанного сертификата
keytool -keystore $DIR/$CLIENT_NAME.keystore.jks \
    -alias $CLIENT_NAME \
    -import -file $CLIENT_NAME-cert-signed \
    -storepass $PASSWORD \
    -keypass $PASSWORD \
    -noprompt

# Копирование truststore
cp $DIR/kafka.truststore.jks $DIR/$CLIENT_NAME.truststore.jks

# Очистка
rm -f $DIR/$CLIENT_NAME.csr $DIR/$CLIENT_NAME-cert-signed

echo "Готово! Файлы:"
echo "- $CLIENT_NAME.keystore.jks (клиентский сертификат)"
echo "- $CLIENT_NAME.truststore.jks (доверенные CA)"
echo ""
echo "Пароль: $PASSWORD"