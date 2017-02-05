<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <title>Вход</title>
        <meta name="description" content="">
        <meta name="author" content="">

        <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
        <!--[if lt IE 9]>
          <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
        <![endif]-->

        <link href="<@spring.url '/static/components/bootstrap/css/bootstrap.css?2.0.4' />" rel="stylesheet">

        <style type="text/css">
            body {
                padding-top: 10px !important;
            }
        </style>
    </head>
    <body class="bootstrap">
        <div class="container" id="moderation">
            <div class="page-header">
                <h1>Вход</h1>
            </div>
            <div class="row">
                <div class="span7">
                <#if ticket?? >
                    <div class="alert alert-info">
                        Текущее значение тикета: ${ticket} &nbsp;&nbsp;&nbsp;<a href="<@spring.url '/demo/logout' />">выйти</a>
                    </div>
                </#if>
                <#if model?? >
                    <@spring.bind 'model' />
                    <#if !model.login?? || !model.password?? >
                    <div class="alert alert-error">
                        Укажите логин и пароль
                    </div>
                    <#elseif spring.status.error >
                    <div class="alert alert-error">
                        Неверное имя пользователя или пароль
                    </div>
                    </#if>
                </#if>
                </div>
            </div>
            <div class="row">
                <div class="span">
                    <form action="" method="POST">
                        <div class="clearfix">
                            <label for="login">Логин</label>
                            <div class="input">
                                <input type="text" name="login" />
                            </div>
                        </div>
                        <div class="clearfix">
                            <label for="password">Пароль</label>
                            <div class="input">
                                <input type="password" name="password" />
                            </div>
                        </div>
                        <div class="actions">
                            <input type="submit" value="Войти" class="btn primary" />
                        </div>
                    </form>
                </div>
            </div>
            <p><a href="<@spring.url '/' />">На главную</a></p>
        </div>
    </body>
</html>