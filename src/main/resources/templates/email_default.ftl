<html>
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
        <div
            style="
                font-family: Arial, FreeSans, Helvetica, sans-serif;
                text-align: -webkit-center;
                border-style: solid;
                border-radius: 8px;
                margin: 20px 30% 20px auto;
                padding: 40px 20px;
                width: 70%;
                max-width: fit-content;"
        >
            <div style="display: block;">
                <img src="cid:logoImage" style="width: 200px; margin: auto;" />
                <br />
                <span style="font-weight: bold;font-size: 24px;">
                    ${messageTitle}
                </span>
            </div>
            <hr />
            <#if primaryMessage??>
                <div style="font-size: 16px; max-width: 540px;">
                    <#if fistName??>
                        Hi ${firstName}
                        <#if lastName??>
                            ${lastName}
                        </#if>
                        ,<br />
                    </#if>
                    ${primaryMessage}
                </div>
            </#if>
            <#if buttonLabel?? && buttonLink??>
                <br />
                <a
                    href="${buttonLink}"
                    style="
                        font-size: 18px;
                        background-color: #062f6d;
                        border-style: solid;
                        border-radius: 8px;
                        color: white;
                        padding: 10px 15px;
                        text-align: center;
                        text-decoration: none;
                        display: inline-block;"
                >${buttonLabel}</a>
            </#if>
            <hr />
            <div style="font-family: Arial, FreeSans, Helvetica, sans-serif;font-size: 16px;">
                Copyright (c) Salesfox 2020
            </div>
        </div>
    </body>
</html>
