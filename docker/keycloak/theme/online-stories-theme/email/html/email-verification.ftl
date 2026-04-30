<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác thực tài khoản NovelToolKit</title>
    <style>
        /* Reset cơ bản cho email để không bị vỡ giao diện trên điện thoại */
        body, table, td, a { -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }
        table, td { mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
        img { -ms-interpolation-mode: bicubicO; border: 0; height: auto; line-height: 100%; outline: none; text-decoration: none; }
        body { margin: 0; padding: 0; width: 100% !important; background-color: #ffffff; font-family: Arial, sans-serif; }
    </style>
</head>
<body style="background-color: #ffffff; margin: 0; padding: 40px 0;">

<table border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e0e0e0; border-radius: 4px; overflow: hidden;">

    <!-- Header -->
    <tr>
        <td align="center" style="background-color: #272863; padding: 35px 20px;">
            <h1 style="margin: 0; color: #ffffff; font-size: 24px; font-weight: bold;">📚 NovelToolKit</h1>
        </td>
    </tr>

    <!-- Body -->
    <tr>
        <td style="padding: 40px 30px; color: #1a1a1a; line-height: 1.6;">
            <!-- Keycloak tự động điền Tên hoặc Username của người dùng -->
            <h2 style="margin: 0 0 20px 0; font-size: 20px;">Xin chào, ${user.firstName!user.username!'bạn'}! 👋</h2>

            <p style="margin: 0 0 15px 0;">Cảm ơn bạn đã tham gia NovelToolKit. Chúng tôi rất vui mừng khi có bạn!</p>

            <p style="margin: 0 0 30px 0;">Để hoàn tất đăng ký và kích hoạt tài khoản, vui lòng xác minh địa chỉ email của bạn bằng cách nhấp vào nút bên dưới:</p>

            <!-- Nút Xác thực với biến link của Keycloak -->
            <div style="text-align: center; margin: 40px 0;">
                <a href="${link}" style="background-color: #dcd6fa; color: #4437a3; padding: 14px 32px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 16px; display: inline-block;">Xác thực tài khoản</a>
            </div>

            <!-- Keycloak tự động điền thời gian hết hạn của link -->
            <p style="margin: 0 0 15px 0; font-size: 14px;">Liên kết này sẽ hết hạn trong vòng ${linkExpiration} phút. Nếu bạn không tạo tài khoản với NovelToolKit, vui lòng bỏ qua email này.</p>

            <p style="margin: 0; font-size: 14px;">Sau khi xác minh, bạn sẽ có quyền truy cập đầy đủ để khám phá và chia sẻ những câu chuyện tuyệt vời!</p>
        </td>
    </tr>

    <!-- Footer -->
    <tr>
        <td align="center" style="background-color: #f8f9fa; padding: 30px 20px; font-size: 12px; color: #6c757d; border-top: 1px solid #eeeeee;">
            <p style="margin: 0 0 10px 0;">&copy; 2026 NovelToolKit. Tất cả các quyền được bảo lưu.</p>
            <p style="margin: 0 0 10px 0;">Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với đội hỗ trợ tại <a href="mailto:support@onlinestories.com" style="color: #4437a3; text-decoration: underline;">support@onlinestories.com</a></p>
            <p style="margin: 0; font-style: italic;">Đây là tin nhắn tự động, vui lòng không trả lời email này.</p>
        </td>
    </tr>

</table>

</body>
</html>