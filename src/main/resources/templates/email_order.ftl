<html>
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
        <div style="display: block;">
            <img src="cid:logoImage" />
            <br />
            <span style="font-weight: bold;font-size: 24px;">
                ${messageTitle}
            </span>
        </div>
        <hr />
        <#if primaryMessage??>
            <div style="font-size: 16px; max-width: 720px;">
                ${primaryMessage}
            </div>
        </#if>
    </body>
</html>
