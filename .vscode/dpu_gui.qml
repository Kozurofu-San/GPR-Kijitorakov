import QtQuick
import QtQuick.Controls
import QtQuick.Layouts
 
import StwMcc 1.0
 
Rectangle {
    id: root
    anchors.fill: parent
    color: "lightblue"
 
    property int deviceId: 16
 
    // Device - компонент для получения данных от устройства без отрисовки дополнительных элементов
    // Позволяет отображать значения в собственных компонентах
    Device {
        id: device
        deviceId: root.deviceId // Id устройства, от которого получать телеметрию
        tmParams: [ // Список телеметрических параметров, которые нужно запрашивать
            TmParam {
                path: "core.can.canStats.can.can1.sendToTxQueue" // Полное имя телеметрического сообщения
            }
        ]
    }
 
    ColumnLayout {
        anchors.fill: parent  
 
        // Пользовательский компонент для визуализации значений телеметрии из компонента Device
        Label {
            text: 'Значение sendToTxQueue: ' + device.tmParams[0].value
        }
 
        // Пользовательский компонент для отправки команд с помощью компонента Device
        Button {
            text: "Включить телеметрию core.can.canStats"
            onClicked: {
                // sendCmd - метод компонента Device с параметрами sendCmd(QString pathToCmd, QVariantList argList)
                // pathToCmd - полный путь до команды, которую нужно отправить
                // argList - список аргументов, которые нужны для отправки команды. Может быть пустым
                // Например:
                // pathToCmd = "core.tm.enableMsg" - отправка команды включения телеметрии
                // argList = [3950211540, "Ordinary"] , где
                // 3950211540 - msgId сообщения которое нужно включить
                // "Ordinary" - частота запроса сообщения
                device.sendCmd("core.tm.enableMsg", [3950211540, "Ordinary"])
            }
        }
        // Компонент-текст, отобржающий текущее значение телеметрического сообщения с заданным периодом
        MccTmLabel {
            width: 150
            height: 40
 
            deviceId: root.deviceId // Id устройства, от которого получать телеметрию
            path: "core.can.canStats.can.can1.sendToTxQueue" // Полное имя телеметрического сообщения
            lifetime: 2 // Период обновления в секундах
            pointSize: 8 // Размер шрифта
            precision: 4 // Количество знаков после запятой
        }
        // Компонент-кнопка для отправки определённых команд на устройство
        MccCmd {
            width: 150
            height: 40
 
            deviceId: root.deviceId // Id устройства, от которого получать телеметрию
            path: "core.can.getCanInfo" // Полное имя команды
            argList: [] // Список аргументов для комманды
            description: "Отправить команду" // Текст кнопки
        }
    }
}