<%@ page import="ru.job4j.dream.model.User" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Upload</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js"></script>
</head>
<body>

<div class="container">
    <div class="row">
        <ul class="nav">
            <li class="nav-item">
                <a class="nav-link" href="<%=request.getContextPath()%>/login.jsp">
                    <% User user = (User) session.getAttribute("user"); %>
                    <% if (user == null) { %>
                    Неизсветный пользователь | Войти
                    <% } else { %>
                    <c:out value="Текущий пользователь: ${user.name}"/> | Выйти</a>
                <%} %>
            </li>
        </ul>
    </div>
    <h2>Загрузка фотографии</h2>
    <form action="<c:url value='/upload?id=${param.id}'/>" method="post" enctype="multipart/form-data">
        <div class="checkbox">
            <input type="file" name="file">
        </div>
        <button type="submit" class="btn">Загрузить</button>
    </form>
</div>

</body>
</html>