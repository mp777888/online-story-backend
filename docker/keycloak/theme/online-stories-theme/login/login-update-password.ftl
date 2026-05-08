<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cập nhật mật khẩu - NovelToolKit</title>
    <style>
        body { margin: 0; padding: 0; background-color: #f4f4f9; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; }
        .card { background-color: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); max-width: 400px; width: 100%; box-sizing: border-box; }
        .logo { color: #272863; font-size: 28px; font-weight: bold; margin-bottom: 20px; text-align: center; display: block; }
        h1 { font-size: 20px; color: #333; text-align: center; margin-top: 0; margin-bottom: 25px; }
        .form-group { margin-bottom: 18px; text-align: left; }
        label { display: block; margin-bottom: 6px; color: #555; font-size: 14px; font-weight: bold; }
        input[type="password"] { width: 100%; padding: 12px; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; font-size: 14px; transition: border-color 0.3s; }
        input[type="password"]:focus { border-color: #272863; outline: none; }
        .checkbox-group { display: flex; align-items: center; margin-bottom: 25px; font-size: 14px; color: #555; text-align: left; }
        .checkbox-group input { margin-right: 8px; width: 16px; height: 16px; cursor: pointer; }
        .checkbox-group label { margin-bottom: 0; font-weight: normal; cursor: pointer; }
        .btn { width: 100%; background-color: #272863; color: white; padding: 14px; border: none; border-radius: 6px; font-size: 16px; font-weight: bold; cursor: pointer; transition: background 0.3s; }
        .btn:hover { background-color: #1a1a4b; }
        .error { color: #d9534f; background-color: #fdf7f7; border: 1px solid #d9534f; padding: 12px; border-radius: 6px; margin-bottom: 20px; font-size: 14px; text-align: left; }
    </style>
</head>
<body>

<div class="card">
    <div class="logo">📚 NovelToolKit</div>
    <h1>Tạo mật khẩu mới</h1>

    <#if message?has_content && message.type == 'error'>
        <div class="error">
            ${message.summary}
        </div>
    </#if>

    <form action="${url.loginAction}" method="post">

        <div class="form-group">
            <label for="password-new">Mật khẩu mới</label>
            <input type="password" id="password-new" name="password-new" placeholder="Nhập mật khẩu mới" required autofocus autocomplete="new-password" />
        </div>

        <div class="form-group">
            <label for="password-confirm">Xác nhận mật khẩu</label>
            <input type="password" id="password-confirm" name="password-confirm" placeholder="Nhập lại mật khẩu" required autocomplete="new-password" />
        </div>

        <div class="checkbox-group">
            <input type="checkbox" id="logout-sessions" name="logout-sessions" value="on" checked>
            <label for="logout-sessions">Đăng xuất khỏi các thiết bị khác</label>
        </div>

        <button type="submit" class="btn">Lưu mật khẩu</button>
    </form>
</div>

</body>
</html>