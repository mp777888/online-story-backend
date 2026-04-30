<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông báo - Online Stories</title>
    <style>
        body { margin: 0; padding: 0; background-color: #f4f4f9; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; }
        .card { background-color: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); text-align: center; max-width: 400px; width: 90%; }
        .logo { color: #272863; font-size: 28px; font-weight: bold; margin-bottom: 20px; display: block; }
        .message { font-size: 18px; color: #333; margin-bottom: 20px; line-height: 1.5; }
        .btn { background-color: #272863; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; transition: background 0.3s; }
        .btn:hover { background-color: #1a1a4b; }
        .sub-text { font-size: 13px; color: #777; margin-top: 20px; }
    </style>
</head>
<body>

<div class="card">
    <div class="logo">📚 Online Stories</div>

    <!-- Keycloak sẽ điền câu thông báo "Your email address has been verified" vào biến này -->
    <div class="message">
        ${message.summary}
    </div>

    <!-- Nút bấm quay lại Frontend -->
    <#if pageRedirectUri?has_content>
        <a href="${pageRedirectUri}" class="btn">Quay lại Ứng dụng</a>
        <div class="sub-text">Đang tự động chuyển hướng sau <span id="countdown">3</span> giây...</div>

        <!-- Script tự động chuyển hướng -->
        <script>
            let count = 3;
            const timer = setInterval(function() {
                count--;
                document.getElementById('countdown').innerText = count;
                if (count <= 0) {
                    clearInterval(timer);
                    window.location.href = "${pageRedirectUri}"; // Chuyển thẳng về React/Vue
                }
            }, 1000);
        </script>
    <#elseif actionUri?has_content>
        <a href="${actionUri}" class="btn">Tiếp tục</a>
    </#if>
</div>

</body>
</html>