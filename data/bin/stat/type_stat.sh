#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"

function BAR {
    echo === $2 ===
    awk "/$1/{print \$1}" | bar_chart.py -k
    echo ""
}

BAR sFOUNDER_INDIVIDUAL "Учредители ФЛ" < $@
BAR dFOUNDER_INDIVIDUAL "Учрежденные компании ФЛ" < $@

BAR sFOUNDER_COMPANY "Учредители-компании" < $@
BAR dFOUNDER_COMPANY "Учрежденные компании ЮЛ" < $@

BAR sEXECUTIVE_INDIVIDUAL "Руководители ФЛ" < $@
BAR dEXECUTIVE_INDIVIDUAL "Руководимые компании ФЛ" < $@

BAR sADDRESS "Адреса ЮЛ" < $@
BAR dADDRESS "Компании по адресу" < $@

BAR sPHONE "Телефоны" < $@
BAR dPHONE "Компании по телефону" < $@

BAR sEXECUTIVE_COMPANY "Управляющие компании" < $@
BAR dEXECUTIVE_COMPANY "Руководимые компании ЮЛ" < $@

BAR sHEAD_COMPANY "Головные компании" < $@
BAR dHEAD_COMPANY "Филиалы" < $@

BAR sPREDECESSOR_COMPANY "Предшественники" < $@
BAR dPREDECESSOR_COMPANY "Правоприемники" < $@

