package me.soilmonitoring.iam.services;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HtmlTemplateService {

    public String buildLoginPage(String clientId, String scope, String error) {
        String errorHtml = "";
        if (error != null) {
            errorHtml = "<div id='error' class='error' style='display:block;'>Invalid username or password</div>";
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Login - Soil Monitoring IAM</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                }
                .login-container {
                    background: white;
                    padding: 40px;
                    border-radius: 10px;
                    box-shadow: 0 10px 25px rgba(0,0,0,0.2);
                    width: 100%%;
                    max-width: 400px;
                }
                h1 { text-align: center; color: #333; margin-bottom: 10px; }
                .subtitle { text-align: center; color: #666; font-size: 14px; margin-bottom: 30px; }
                .form-group { margin-bottom: 20px; }
                label { display: block; margin-bottom: 5px; color: #555; font-weight: 500; }
                input[type="text"], input[type="password"] {
                    width: 100%%;
                    padding: 12px;
                    border: 1px solid #ddd;
                    border-radius: 5px;
                    font-size: 14px;
                    box-sizing: border-box;
                }
                input:focus { outline: none; border-color: #667eea; }
                button {
                    width: 100%%;
                    padding: 12px;
                    background: #667eea;
                    color: white;
                    border: none;
                    border-radius: 5px;
                    font-size: 16px;
                    font-weight: 600;
                    cursor: pointer;
                }
                button:hover { background: #5568d3; }
                .error {
                    background: #fee;
                    border: 1px solid #fcc;
                    color: #c00;
                    padding: 10px;
                    border-radius: 5px;
                    margin-bottom: 20px;
                    display: none;
                }
                .info {
                    background: #e3f2fd;
                    padding: 10px;
                    border-radius: 5px;
                    margin-bottom: 20px;
                    font-size: 13px;
                }
            </style>
        </head>
        <body>
            <div class="login-container">
                <h1>🌱 Soil Monitoring</h1>
                <p class="subtitle">Sign in to continue</p>
                
                <div class="info">
                    <strong>Application:</strong> %s<br>
                    <strong>Requested access:</strong> %s
                </div>
                
                %s
                
                <form method="POST" action="/iam/login/authorization">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username" required autofocus>
                    </div>
                    
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" required>
                    </div>
                    
                    <button type="submit">Sign In</button>
                </form>
            </div>
        </body>
        </html>
        """.formatted(clientId, scope != null ? scope : "profile, email", errorHtml);
    }

    public String buildConsentPage(String clientId, String scope, String username) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Grant Access - Soil Monitoring IAM</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    min-height: 100vh;
                    margin: 0;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                }
                .consent-container {
                    background: white;
                    padding: 40px;
                    border-radius: 10px;
                    box-shadow: 0 10px 25px rgba(0,0,0,0.2);
                    max-width: 500px;
                }
                h1 { text-align: center; color: #333; }
                .subtitle { text-align: center; color: #666; font-size: 14px; margin-bottom: 30px; }
                .user-info { background: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 25px; }
                .permission-item {
                    padding: 10px;
                    margin: 10px 0;
                    background: #f8f9fa;
                    border-left: 3px solid #667eea;
                }
                .button-group { display: flex; gap: 10px; margin-top: 30px; }
                button {
                    flex: 1;
                    padding: 12px;
                    border: none;
                    border-radius: 5px;
                    font-size: 16px;
                    font-weight: 600;
                    cursor: pointer;
                }
                .btn-approve { background: #667eea; color: white; }
                .btn-approve:hover { background: #5568d3; }
                .btn-deny { background: #e0e0e0; color: #333; }
            </style>
        </head>
        <body>
            <div class="consent-container">
                <h1>🌱 Grant Access</h1>
                <p class="subtitle">%s wants to access your account</p>
                
                <div class="user-info">
                    <strong>Logged in as:</strong> %s
                </div>
                
                <div class="permissions">
                    <p><strong>This application will be able to:</strong></p>
                    <div class="permission-item">✓ View your profile information</div>
                    <div class="permission-item">✓ Access your email address</div>
                    <div class="permission-item">✓ Manage your fields and sensors</div>
                </div>
                
                <form method="POST" action="/iam/consent">
                    <input type="hidden" name="approved_scope" value="%s">
                    <input type="hidden" name="username" value="%s">
                    
                    <div class="button-group">
                        <button type="submit" name="approval_status" value="YES" class="btn-approve">
                            ✓ Allow Access
                        </button>
                        <button type="submit" name="approval_status" value="NO" class="btn-deny">
                            ✗ Deny
                        </button>
                    </div>
                </form>
            </div>
        </body>
        </html>
        """.formatted(clientId, username, scope, username);
    }

    public String buildErrorPage(String error, String description) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Error - Soil Monitoring IAM</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    background: #f5f5f5;
                }
                .error-container {
                    background: white;
                    padding: 40px;
                    border-radius: 10px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    text-align: center;
                }
                h1 { color: #c00; }
            </style>
        </head>
        <body>
            <div class="error-container">
                <h1>❌ Error</h1>
                <p><strong>%s</strong></p>
                <p>%s</p>
            </div>
        </body>
        </html>
        """.formatted(error, description);
    }
}