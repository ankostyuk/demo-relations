<#include "/snippets/common.ftl" />

<!doctype html>
<html lang="ru">
    <head>
        <meta charset="utf-8">

        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="">
        <meta name="author" content="">
        <meta name="fragment" content="!">
    </head>

    <body>
        <script>
            console.info('UI-3.0 export...');

            window._INTERNAL_DATA_ = window._EXPORT_DATA_ = {
                isInternalExport: true,

                report:         ${report_json},
                nodeTypes:      ${node_types_json},
                relationTypes:  ${relation_types_json},

                internalExportReportReady: function() {
                    window._REPORT_READY_ = true;
                }
            };
        </script>

        <!-- session.js -->
        <script type="text/javascript">
            window.session = {
                options: {
                    gapi_location: false,
                    location_cookie: null
                }
            };
        </script>

        <!-- requirejs -->
        <script src="//cdnjs.cloudflare.com/ajax/libs/require.js/2.1.16/require.min.js"></script>

        <!-- nkb-app -->
        <script src="<@spring.url _relationUIPath + 'main.js?${_relationUIBuildId}' />"></script>
    </body>
</html>
