# creditnet-relations-webapp

> Веб-приложение "Связи компаний"

## Окружение

* Java 6
* Maven 3
* node.js 0.10.x+
* npm 1.3.x+
* grunt-cli `npm install grunt-cli -g`
* bower `npm install bower -g`

## Известные баги

Если среда разработки говорит

    Failed to execute goal org.apache.maven.plugins:maven-antrun-plugin:1.7:run (grunt-clean) on project creditnet-relations-webapp: An Ant BuildException has occured: Execute failed: java.io.IOException: Cannot run program "grunt": error=2, Нет такого файла или каталога
    around Ant part ...<exec dir="/home/alexander/projects/java/nkb/nkbrelation/webapp" executable="grunt" failonerror="false">... @ 4:107 in /home/alexander/projects/java/nkb/nkbrelation/webapp/target/antrun/build-main.xml
    -> [Help 1]

то надо вписать

    PATH DEFAULT=${PATH}:/home/user/.nvm/v0.10.23/bin/

в файл `~/.pam_environment`, заменив имя пользователя и версию `nvm` на актуальные.

Проблема в Ubuntu заключается в том, что при локальной установке `nodejs` через [nvm](https://github.com/creationix/nvm) и запуске среды из Unity,
не применяется файл `~/.bash_rc`. Смотри [EnvironmentVariables](https://help.ubuntu.com/community/EnvironmentVariables)

## Поддержка фронтенда

Фронтенд: [nullpointer-relation-ui](https://github.com/newpointer/relation-ui)

Фронтенд для публичного доступа в продакшене деплоится внешним образом,
однако инициализация фронтенда в данном веб-приложении необходима:
* для функционирования экспорта отчетов
* для тестового просмотра функциональности бекенд-разработчиками

Для обновления версии фронтенда править в файле `bower.json`:

```json
    "nullpointer-relation-ui": "git@github.com:newpointer/relation-ui.git#x.x.x"
```

где `x.x.x` - версия

Для инициализации фронтенда "вручную" выполнить:

    npm install
    grunt init

Это необходимо при первоначальной maven-сборке проекта с профилями `dev`, `dev-skip-tests`, ...

При maven-сборках профилей `production`, `testing` инициализация фронтенда будет выполнена автоматически.

Доступ к фронтенду в веб-приложении: http://localhost:8084/nkbrelation/report

## Очистка

Удаление зависимостей, сборки

    mvn clean
    grunt cleanup
