<#include "/snippets/common.ftl" />

<!doctype html>
<html lang="ru">
    <head>
        <meta charset="utf-8">

        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="">
        <meta name="author" content="">
        <meta name="fragment" content="!">

        <title>Анализ связей</title>
    </head>
    <body>
        <h1>Анализ связей</h1>
        <p><a href="<@spring.url '/demo/login' />">Демо-вход</a></p>
        <h3>Примеры</h3>
        <p><a href="<@spring.url _relationUIPath + 'index.html' />">Новый аналитический отчёт</a></p>
        <p><a href="<@spring.url _relationUIPath + 'index.html?node.type=COMPANY&chief.contains=малеев' />">Исследовать компании с руководителем "Малеев"</a></p>
        <p><a href="<@spring.url _relationUIPath + 'index.html?node.type=COMPANY&bsn_id.equals=1055559' />">Исследовать компанию c bsn_id = 1055559</a></p>
        <p><a href="<@spring.url _relationUIPath + 'index.html?node.type=COMPANY&bsn_id.equals=982962' />">Исследовать компанию ОАО АНК "Башнефть"</a></p>
    </body>
</html>
