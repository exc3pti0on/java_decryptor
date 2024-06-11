# Дешифратор паролей из профилей пользователя приложений Mozilla
## How to use

Стандартный запуск программы:
```
decryptor.exe
```
Запуск программы с указанием пути к профилю, находящегося не по умолчанию:
```
decryptor.exe /data_folder/profiles.ini
```
Экспорт паролей в форматах, используя флаг --format:
```
decryptor.exe --format csv
```
```
decryptor.exe --format json
``` 
```
decryptor.exe --format human
```
```
decryptor.exe --format tabular
```
При необходимости, пароли можно экспортировать в Pass: 
```
decryptor.exe --format pass
```
Для добавления к имени пользователя префикс login, для совместимости с расширением браузера, вы можете использовать:
```
decryptor.exe --format pass --pass-username-prefix "login:"
```
При возникновении проблемы рекомендуется запустить decryptor.exe в режиме высокой детализации, вызвав его с помощью:
```
decryptor.exe -vvv
```
