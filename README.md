#Задание

От пользователя в MQ очередь REQUEST приходит запрос на осуществление платежа, пример которого можно посмотреть в файле: payment.xml.

1. Осуществить над сообщением следующие проверки:
Номер счета по дебету (поле Debit) не совпадает с номером по кредиту (поле Credit).
Время в поле ObjectLastChanged не отличается от текущего более, чем на N минут. Формат ObjectLastChanged описан в  http://www.w3.org/TR/NOTE-datetime. Число N - настраиваемый на BAR-файле параметр
(если зависает debug toolkit используйте trace c уровнем debug).
Поле MessageId находится в каноническом формате для UUID (http://en.wikipedia.org/wiki/Universally_unique_identifier, Definition, canonical form).
1.1 Если проверка завершается с ошибкой:
поместить сообщение в очередь ERRORS, добавив в сообщение служебный заголовок MQRFH2 и в его подпапку usr записать в поле errorDescription описание ошибки.
Завершить обработку.

##После проверки, нужно послать сообщение в 3 системы:
 
##2.1. Система 1.
Ожидает сообщение в очереди SYSTEM1.
Система ожидает сообщение в следующем виде (в квадратных скобках - из какого поля исходного сообщения брать значение и что делать с форматом):
(про преобразование форматов дат в ESQL: Help -> Formatting and parsing dateTimes as strings)

Тестовый фреймворк для обучения IIB.

```
<?xml version="1.0" encoding="UTF-8"?>
<msg:Payment xmlns:msg="http://message.system1.com" xmlns:hd="http://header.system1.com" xmlns:bd="http://body.system1.com">
                <msg:Metadata messageId="[MessageId]">
                               <hd:Version>[Version]</hd:Version>
                               <hd:SourceSystem>[Source]</hd:SourceSystem>
                               <hd:LastChanged>[время из ObjectLastChanged в формате ГГ-ММ-ДДЧЧ-СС в Нью-Йоркском часовом поясе(преобразование часового пояса реализовать с помощью java)]</hd:LastChanged>
                </msg:Metadata>
                <msg:Body>
                               <bd:OrderNo>[OrderNo]</bd:OrderNo>
                               <bd:OrderDate>[OrderDate] в формате ГГ-ДД-ММ</bd:OrderDate>
                               <bd:DebitAccount>[Debit]</bd:DebitAccount>
                               <bd:CreditAccount>[Credit]</bd:CreditAccount>
                               <bd:Amount>[RubAmt]</bd:Amount>
                </msg:Body>
</msg:Payment>
```
Квадратные скобки в сообщение добавлять не надо.

##2.2. Система 2.

Ожидает сообщение в очереди, название которой формируется по правилу: "SYSTEM2.FROM." + содержимое поля Source. Предполагается, что набор очередей ограничен и известен изначально.

Нужно сохранить pretty-print форматирование исходного сообщения (переводы каретки между элементами).

Сообщение должно быть преобразовано следующим образом:
- в поле Priority служебного заголовка MQMD записать значение из поля Priority сообщеня

- добавить в сообщение служебный заголовок MQRFH2 и в его элемент usr записать следующие поля: orderNo, rubAmt со значениями соответствующих полей сообщения (OrderNo, RubAmt) . Обратите внимание, что заголовок MQRFH2 располагается между MQMD и телом.

- преобразовать кодировку сообщения из UTF-8 в другую (значение в виде кода ccsid задается на bar файле), из заголовка XML убрать информацию о предыдущей кодировке.

##2.3. Система 3.

Ожидает сообщение в очереди SYSTEM3.
Сообщение должно быть преобразовано следующим образом:
-поле Digest должно быть преобразовано из Base64 (http://en.wikipedia.org/wiki/Base64) в обычный текст (предполагается, что текст был в кодировке UTF-8).

-поля прямые потомки поля PaymentOrderRub должны быть отсортированы по алфавиту по названию поля

-все поля должны быть в нулевом неймспейсе (то есть не <mb:OrderNo> а <orderno>)

-каждому элементу нужно добавить атрибут level со значением его уровня в XML дереве. У поля Message уровень 0, у полей MessageHeader и MessageBod уровень 1 и т.д.

2.4. Система 4. 

Необходимо реализовать при помощи JavaCompute узла следующую логику:

Система ожидает 2 сообщения в очередях SYSTEM4.DEBIT и SYSTEM4.CREDIT.
Система ожидает сообщения в следующем виде (в квадратных скобках - из какого поля исходного сообщения брать значение и что делать с форматом):

```
<?xml version="1.0" encoding="UTF-8"?>
<msg:Payment xmlns:msg="http://message.system4.com" xmlns:hd="http://header.system4.com" xmlns:bd="http://body.system4.com">
               <msg:Header>
                              <hd:MessageId>[MessageId]</hd:MessageId>
                              <hd:Type>Debit или Credit</hd:Type>
                              <hd:Version>[Version]</hd:Version>
                              <hd:SourceSystem>[Source]</hd:SourceSystem>
               </msg:Header>
               <msg:Body>
                              <bd:OrderNo>[OrderNo]</bd:OrderNo>
                              <bd:OrderDate>[ValueDate] в формате ГГ-ДД-ММ</bd:OrderDate>
                              <bd:Account>В одном сообщении [Debit], в другом [Credit]</bd:Account>
                              <bd:Amount>[RubAmt]</bd:Amount>
               </msg:Body>
</msg:Payment>
```

В поле CorrelId служебного заголовка MQMD записать значение 414D512045534244455630312E514D20B98A765705F2102F
Добавить в сообщение служебный заголовок MQRFH2 и в его элемент usr записать поле [Digest]