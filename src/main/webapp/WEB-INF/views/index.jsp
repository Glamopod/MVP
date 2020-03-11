<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<title>
INDEX JSP
</title>
<head></head>
<body>
<br>
timestampAsInteger: ${timestampAsInteger}
<br>
dataFromTimestamp: ${dataFromTimestamp}
<br>
base currency: ${base_currency}
<br>
rates: ${rates}

<form method="post">
Zahl 1: <input type="text" name="zahl1">
Operation: <input type="text" name="operation">
Zahl 2: <input type="text" name="zahl2">
Ergebnis: ${result}
<br>
<input type="submit" id="button">
</form>

</body>
</html>
<br>
<%
int a = 5;
int b = 11;
out.println("Hallo Welt: " + (a+b));
%>
