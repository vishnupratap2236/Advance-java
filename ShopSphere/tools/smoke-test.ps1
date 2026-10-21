<#
  End-to-end smoke test of the deployed ShopSphere application.

  It drives a real browser-like session over HTTP: registration, adding to the
  cart, checkout, order history and the administration status update.

  Usage:
    powershell -ExecutionPolicy Bypass -File tools\smoke-test.ps1 `
        -BaseUrl http://localhost:8080/shopsphere `
        -AdminEmail admin@shopsphere.local -AdminPassword <password>
#>
param(
    [string]$BaseUrl = 'http://localhost:8080/shopsphere',
    [string]$AdminEmail = '',
    [string]$AdminPassword = '',
    [switch]$SkipAdmin
)

$ErrorActionPreference = 'Stop'
$script:pass = 0
$script:fail = 0

function Write-Result([string]$Name, [bool]$Ok, [string]$Detail = '') {
    if ($Ok) {
        $script:pass++
        Write-Host ("  PASS  {0}{1}" -f $Name, $(if ($Detail) { " ($Detail)" } else { '' })) -ForegroundColor Green
    } else {
        $script:fail++
        Write-Host ("  FAIL  {0}{1}" -f $Name, $(if ($Detail) { " ($Detail)" } else { '' })) -ForegroundColor Red
    }
}

function Get-CsrfToken([Microsoft.PowerShell.Commands.WebRequestSession]$Session, [string]$Path) {
    $response = Invoke-WebRequest -Uri ($BaseUrl + $Path) -WebSession $Session -UseBasicParsing -TimeoutSec 30
    $match = [regex]::Match($response.Content, 'name="_csrf" value="([^"]+)"')
    if (-not $match.Success) {
        throw "No CSRF token found on $Path"
    }
    return $match.Groups[1].Value
}

function Invoke-Post([Microsoft.PowerShell.Commands.WebRequestSession]$Session, [string]$Path,
                     [hashtable]$Body) {
    # A redirect is answered with its own status code, so the redirect is not followed.
    $response = Invoke-WebRequest -Uri ($BaseUrl + $Path) -WebSession $Session -UseBasicParsing -TimeoutSec 30 `
        -Method POST -Body $Body -MaximumRedirection 0 -ErrorAction SilentlyContinue
    if ($response) {
        return $response
    }
    $errorResponse = $Error[0].Exception.Response
    if ($errorResponse) {
        return [pscustomobject]@{ StatusCode = [int]$errorResponse.StatusCode; Headers = $errorResponse.Headers; Content = '' }
    }
    throw $Error[0]
}

Write-Host ""
Write-Host "ShopSphere smoke test against $BaseUrl" -ForegroundColor Cyan
Write-Host "-----------------------------------------"

# ---------------------------------------------------------------- public pages
Write-Host "`n[1] Public pages"
foreach ($path in @('/', '/products', '/products?category=audio', '/product?id=1', '/login', '/register')) {
    $response = Invoke-WebRequest -Uri ($BaseUrl + $path) -UseBasicParsing -TimeoutSec 30
    Write-Result "GET $path" ($response.StatusCode -eq 200) "HTTP $($response.StatusCode)"
}

$api = Invoke-WebRequest -Uri ($BaseUrl + '/api/products?pageSize=50') -UseBasicParsing -TimeoutSec 30
$json = $api.Content | ConvertFrom-Json
Write-Result "GET /api/products returns JSON" ($json.items.Count -ge 1) "$($json.items.Count) items"

# The stock of the product used below is restored at the end of the run, so the
# smoke test does not consume the demo catalogue.
$stockBefore = ($json.items | Where-Object { $_.id -eq '1' } | Select-Object -First 1).stock

# ------------------------------------------------------------------- customer
Write-Host "`n[2] Customer journey"
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$email = "smoke.$([guid]::NewGuid().ToString('N').Substring(0,8))@shopsphere.test"
$password = 'SmokeTest123!'

$token = Get-CsrfToken $session '/register'
$register = Invoke-Post $session '/register' @{ _csrf = $token; email = $email; fullName = 'Smoke Tester'; phone = '+91 90000 00000'; password = $password; confirmPassword = $password }
Write-Result "POST /register creates the account" ($register.StatusCode -eq 302) "HTTP $($register.StatusCode)"

$homeResponse = Invoke-WebRequest -Uri ($BaseUrl + '/') -WebSession $session -UseBasicParsing -TimeoutSec 30
Write-Result "the session is authenticated" ($homeResponse.Content -like '*Smoke Tester*')

$token = Get-CsrfToken $session '/product?id=1'
$add = Invoke-Post $session '/cart' @{ _csrf = $token; action = 'add'; productId = '1'; quantity = '2' }
Write-Result "POST /cart adds a product" ($add.StatusCode -eq 302)

$cart = Invoke-WebRequest -Uri ($BaseUrl + '/cart') -WebSession $session -UseBasicParsing -TimeoutSec 30
Write-Result "the cart lists the product" ($cart.Content -like '*Aurora Wireless Headphones*')
Write-Result "the cart shows a server side total" ($cart.Content -like '*Order summary*')

$apiCart = Invoke-WebRequest -Uri ($BaseUrl + '/api/cart') -WebSession $session -UseBasicParsing -TimeoutSec 30
$apiCartJson = $apiCart.Content | ConvertFrom-Json
Write-Result "GET /api/cart returns the cart of the signed in customer" ($apiCartJson.itemCount -ge 1) "$($apiCartJson.itemCount) items"

$token = Get-CsrfToken $session '/checkout'
$order = Invoke-Post $session '/checkout' @{
    _csrf = $token; action = 'placeOrder'; recipientName = 'Smoke Tester'; phone = '+91 90000 00000'
    line1 = '12 MG Road'; line2 = 'Near the park'; city = 'Bengaluru'; state = 'Karnataka'
    postalCode = '560001'; country = 'India'; notes = 'Leave at reception'; paymentMethod = 'COD'
}
Write-Result "POST /checkout places the order" ($order.StatusCode -eq 302) "HTTP $($order.StatusCode)"

$location = $order.Headers['Location']
$orderId = ($location -split 'id=')[-1]
# The Location header may be relative to the server root or absolute.
$origin = ([uri]$BaseUrl).GetLeftPart([System.UriPartial]::Authority)
$confirmationUrl = if ($location -like 'http*') { $location } else { $origin + $location }
$confirmation = Invoke-WebRequest -Uri $confirmationUrl -WebSession $session -UseBasicParsing -TimeoutSec 30
Write-Result "the confirmation page renders" ($confirmation.Content -like '*order is confirmed*')

$orders = Invoke-WebRequest -Uri ($BaseUrl + '/orders') -WebSession $session -UseBasicParsing -TimeoutSec 30
Write-Result "the order appears in the history" ($orders.Content -like '*SS-*')

$token = Get-CsrfToken $session '/product?id=1'
$review = Invoke-Post $session '/review' @{ _csrf = $token; action = 'add'; productId = '1'; rating = '5'; title = 'Great'; comment = 'Works well.' }
Write-Result "POST /review stores a review" ($review.StatusCode -eq 302)

$token = Get-CsrfToken $session '/product?id=1'
$wishlist = Invoke-Post $session '/wishlist' @{ _csrf = $token; productId = '1' }
Write-Result "POST /wishlist saves a product" ($wishlist.StatusCode -eq 302)

# ---------------------------------------------------------------------- admin
if (-not $SkipAdmin -and $AdminEmail -and $AdminPassword) {
    Write-Host "`n[3] Administration"
    $adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $token = Get-CsrfToken $adminSession '/login'
    $login = Invoke-Post $adminSession '/login' @{ _csrf = $token; email = $AdminEmail; password = $AdminPassword }
    Write-Result "POST /login signs the administrator in" ($login.StatusCode -eq 302)

    $dashboard = Invoke-WebRequest -Uri ($BaseUrl + '/admin') -WebSession $adminSession -UseBasicParsing -TimeoutSec 30
    Write-Result "the dashboard renders" ($dashboard.Content -like '*Dashboard*')

    $adminOrders = Invoke-WebRequest -Uri ($BaseUrl + '/admin/orders') -WebSession $adminSession -UseBasicParsing -TimeoutSec 30
    Write-Result "the admin order list renders" ($adminOrders.Content -like '*Order*')

    $token = Get-CsrfToken $adminSession "/admin/orders?id=$orderId"
    $status = Invoke-Post $adminSession '/admin/orders' @{ _csrf = $token; action = 'updateStatus'; id = $orderId; status = 'SHIPPED'; note = 'Handed to the courier' }
    Write-Result "the admin changes the order status" ($status.StatusCode -eq 302)

    $detail = Invoke-WebRequest -Uri ($BaseUrl + "/admin/orders?id=$orderId") -WebSession $adminSession -UseBasicParsing -TimeoutSec 30
    Write-Result "the order shows the new status" ($detail.Content -like '*Shipped*')

    # Give the reserved stock back so that the demo catalogue stays intact.
    $token = Get-CsrfToken $adminSession '/admin/products'
    $restore = Invoke-Post $adminSession '/admin/products' @{ _csrf = $token; action = 'setStock'; id = '1'; stock = $stockBefore }
    Write-Result "the stock of the test product is restored" ($restore.StatusCode -eq 302) "stock=$stockBefore"
} else {
    Write-Host "`n[3] Administration skipped (no administrator credentials given)"
}

# ------------------------------------------------------------------- security
Write-Host "`n[4] Security"
function Get-Status([string]$Url, [string]$Method = 'GET', [string]$Body = '') {
    $arguments = @('-s', '-o', 'NUL', '-w', '%{http_code}', '-X', $Method)
    if ($Body) { $arguments += @('-d', $Body) }
    $arguments += $Url
    return (& curl.exe @arguments).Trim()
}

$status = Get-Status "$BaseUrl/admin"
Write-Result "an anonymous visitor cannot open /admin" ($status -in @('302', '403')) "HTTP $status"

$status = Get-Status "$BaseUrl/cart" 'POST' 'action=add&productId=1&quantity=1'
Write-Result "a POST without a CSRF token is rejected" ($status -eq '403') "HTTP $status"

$status = Get-Status "$BaseUrl/api/cart" 'GET'
Write-Result "an anonymous API read is rejected" ($status -eq '401') "HTTP $status"

$status = Get-Status "$BaseUrl/api/cart" 'POST' 'productId=1&quantity=1'
Write-Result "an anonymous API write is rejected" ($status -in @('401', '403')) "HTTP $status"

$other = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$otherToken = Get-CsrfToken $other '/login'
$otherLogin = Invoke-Post $other '/login' @{ _csrf = $otherToken; email = $email; password = 'WrongPassword1' }
Write-Result "a wrong password is rejected" ($otherLogin.StatusCode -eq 200) "HTTP $($otherLogin.StatusCode)"

Write-Host ""
Write-Host ("Result: {0} passed, {1} failed" -f $script:pass, $script:fail) -ForegroundColor Cyan
if ($script:fail -gt 0) { exit 1 }
exit 0
