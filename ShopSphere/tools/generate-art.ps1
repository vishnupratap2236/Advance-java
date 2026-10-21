<#
  Generates the local placeholder artwork for the demo catalogue.
  The artwork is plain SVG so that the shop never depends on a remote image host.

  Usage:  powershell -ExecutionPolicy Bypass -File tools\generate-art.ps1
#>
param(
    [string]$WebappPath = (Join-Path (Split-Path -Parent $PSScriptRoot) 'src\main\webapp')
)

$ErrorActionPreference = 'Stop'

function New-SvgFile {
    param(
        [string]$Path,
        [string]$Content
    )
    $directory = Split-Path -Parent $Path
    if (-not (Test-Path -LiteralPath $directory)) {
        New-Item -ItemType Directory -Force -Path $directory | Out-Null
    }
    Set-Content -LiteralPath $Path -Value $Content -Encoding UTF8
    Write-Host ("wrote {0}" -f $Path)
}

function New-ProductSvg {
    param(
        [string]$Title,
        [string]$Subtitle,
        [int]$Hue,
        [string]$Glyph
    )
    $hue2 = ($Hue + 40) % 360
    $glyphMarkup = switch ($Glyph) {
        'headphones' { @'
    <g transform="translate(400,268)">
      <path d="M-150 40 v-30 a150 150 0 0 1 300 0 v30" fill="none" stroke="rgba(255,255,255,0.92)" stroke-width="18" stroke-linecap="round"/>
      <rect x="-176" y="34" width="56" height="96" rx="24" fill="rgba(255,255,255,0.95)"/>
      <rect x="120" y="34" width="56" height="96" rx="24" fill="rgba(255,255,255,0.95)"/>
    </g>
'@ }
        'speaker' { @'
    <g transform="translate(400,268)">
      <rect x="-120" y="-150" width="240" height="300" rx="60" fill="rgba(255,255,255,0.94)"/>
      <circle cx="0" cy="-46" r="58" fill="rgba(20,20,40,0.30)"/>
      <circle cx="0" cy="82" r="34" fill="rgba(20,20,40,0.22)"/>
    </g>
'@ }
        'buds' { @'
    <g transform="translate(400,268)">
      <circle cx="-70" cy="-20" r="54" fill="rgba(255,255,255,0.95)"/>
      <circle cx="70" cy="-20" r="54" fill="rgba(255,255,255,0.95)"/>
      <rect x="-96" y="20" width="52" height="120" rx="26" fill="rgba(255,255,255,0.88)"/>
      <rect x="44" y="20" width="52" height="120" rx="26" fill="rgba(255,255,255,0.88)"/>
    </g>
'@ }
        'watch' { @'
    <g transform="translate(400,268)">
      <rect x="-86" y="-128" width="172" height="256" rx="52" fill="rgba(255,255,255,0.90)"/>
      <rect x="-64" y="-100" width="128" height="200" rx="36" fill="rgba(25,25,45,0.30)"/>
      <path d="M-34 20 L-6 56 L40 -14" fill="none" stroke="rgba(255,255,255,0.95)" stroke-width="14" stroke-linecap="round" stroke-linejoin="round"/>
    </g>
'@ }
        'keyboard' { @'
    <g transform="translate(400,268)">
      <rect x="-190" y="-92" width="380" height="184" rx="26" fill="rgba(255,255,255,0.94)"/>
      <g fill="rgba(30,30,50,0.28)">
        <rect x="-160" y="-64" width="52" height="44" rx="10"/><rect x="-96" y="-64" width="52" height="44" rx="10"/>
        <rect x="-32" y="-64" width="52" height="44" rx="10"/><rect x="32" y="-64" width="52" height="44" rx="10"/>
        <rect x="96" y="-64" width="52" height="44" rx="10"/>
        <rect x="-160" y="-8" width="52" height="44" rx="10"/><rect x="-96" y="-8" width="52" height="44" rx="10"/>
        <rect x="-32" y="-8" width="52" height="44" rx="10"/><rect x="32" y="-8" width="52" height="44" rx="10"/>
        <rect x="96" y="-8" width="52" height="44" rx="10"/>
        <rect x="-160" y="48" width="180" height="44" rx="10"/><rect x="32" y="48" width="116" height="44" rx="10"/>
      </g>
    </g>
'@ }
        'stand' { @'
    <g transform="translate(400,268)">
      <path d="M-150 60 L0 -140 L150 60 Z" fill="rgba(255,255,255,0.93)"/>
      <rect x="-176" y="60" width="352" height="26" rx="13" fill="rgba(255,255,255,0.80)"/>
    </g>
'@ }
        'backpack' { @'
    <g transform="translate(400,268)">
      <path d="M-120 120 v-120 a120 120 0 0 1 240 0 v120 a26 26 0 0 1 -26 26 h-188 a26 26 0 0 1 -26 -26 z" fill="rgba(255,255,255,0.94)"/>
      <rect x="-70" y="-40" width="140" height="80" rx="20" fill="rgba(30,30,50,0.24)"/>
      <path d="M-40 -120 a40 40 0 0 1 80 0" fill="none" stroke="rgba(255,255,255,0.95)" stroke-width="16" stroke-linecap="round"/>
    </g>
'@ }
        'monitor' { @'
    <g transform="translate(400,268)">
      <rect x="-190" y="-140" width="380" height="230" rx="20" fill="rgba(255,255,255,0.95)"/>
      <rect x="-166" y="-116" width="332" height="182" rx="12" fill="rgba(25,25,45,0.32)"/>
      <rect x="-26" y="90" width="52" height="70" fill="rgba(255,255,255,0.88)"/>
      <rect x="-96" y="156" width="192" height="20" rx="10" fill="rgba(255,255,255,0.88)"/>
    </g>
'@ }
        'lamp' { @'
    <g transform="translate(400,268)">
      <path d="M-120 40 L-40 -80 L20 40 Z" fill="rgba(255,255,255,0.95)"/>
      <path d="M0 40 L60 140" stroke="rgba(255,255,255,0.95)" stroke-width="18" stroke-linecap="round"/>
      <rect x="-70" y="138" width="150" height="22" rx="11" fill="rgba(255,255,255,0.88)"/>
      <circle cx="-40" cy="-30" r="70" fill="rgba(255,240,180,0.30)"/>
    </g>
'@ }
        'chair' { @'
    <g transform="translate(400,268)">
      <path d="M-110 -160 h220 a30 30 0 0 1 30 30 v120 a30 30 0 0 1 -30 30 h-220 a30 30 0 0 1 -30 -30 v-120 a30 30 0 0 1 30 -30 z" fill="rgba(255,255,255,0.93)"/>
      <rect x="-130" y="46" width="260" height="34" rx="17" fill="rgba(255,255,255,0.85)"/>
      <path d="M0 80 v70" stroke="rgba(255,255,255,0.9)" stroke-width="18" stroke-linecap="round"/>
      <path d="M-90 150 h180" stroke="rgba(255,255,255,0.9)" stroke-width="18" stroke-linecap="round"/>
    </g>
'@ }
        'mat' { @'
    <g transform="translate(400,268)">
      <rect x="-190" y="-70" width="380" height="140" rx="70" fill="rgba(255,255,255,0.93)"/>
      <rect x="-150" y="-40" width="300" height="80" rx="40" fill="rgba(30,30,50,0.20)"/>
    </g>
'@ }
        'dumbbell' { @'
    <g transform="translate(400,268)">
      <rect x="-40" y="-14" width="80" height="28" rx="14" fill="rgba(255,255,255,0.9)"/>
      <rect x="-160" y="-62" width="90" height="124" rx="26" fill="rgba(255,255,255,0.95)"/>
      <rect x="70" y="-62" width="90" height="124" rx="26" fill="rgba(255,255,255,0.95)"/>
    </g>
'@ }
        'plates' { @'
    <g transform="translate(400,268)">
      <circle cx="-70" cy="30" r="120" fill="rgba(255,255,255,0.93)"/>
      <circle cx="-70" cy="30" r="78" fill="rgba(30,30,50,0.16)"/>
      <circle cx="110" cy="-20" r="96" fill="rgba(255,255,255,0.85)"/>
      <circle cx="110" cy="-20" r="60" fill="rgba(30,30,50,0.16)"/>
    </g>
'@ }
        'ssd' { @'
    <g transform="translate(400,268)">
      <rect x="-130" y="-100" width="260" height="200" rx="28" fill="rgba(255,255,255,0.94)"/>
      <rect x="-94" y="-60" width="188" height="96" rx="16" fill="rgba(30,30,50,0.24)"/>
      <circle cx="60" cy="66" r="16" fill="rgba(30,30,50,0.30)"/>
    </g>
'@ }
        default { @'
    <g transform="translate(400,268)">
      <circle r="120" fill="rgba(255,255,255,0.92)"/>
      <circle r="66" fill="rgba(30,30,50,0.20)"/>
    </g>
'@ }
    }

    $safeTitle = [System.Security.SecurityElement]::Escape($Title)
    $safeSubtitle = [System.Security.SecurityElement]::Escape($Subtitle)

    return @"
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 600" width="800" height="600" role="img" aria-label="$safeTitle">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="hsl($Hue, 68%, 52%)"/>
      <stop offset="100%" stop-color="hsl($hue2, 72%, 38%)"/>
    </linearGradient>
  </defs>
  <rect width="800" height="600" fill="url(#g)"/>
  <circle cx="120" cy="90" r="180" fill="rgba(255,255,255,0.10)"/>
  <circle cx="700" cy="520" r="230" fill="rgba(0,0,0,0.08)"/>
  $glyphMarkup
  <text x="400" y="524" text-anchor="middle" font-family="Segoe UI, Helvetica, Arial, sans-serif"
        font-size="34" font-weight="700" fill="#ffffff">$safeTitle</text>
  <text x="400" y="562" text-anchor="middle" font-family="Segoe UI, Helvetica, Arial, sans-serif"
        font-size="20" fill="rgba(255,255,255,0.82)">$safeSubtitle</text>
</svg>
"@
}

function New-CategorySvg {
    param(
        [string]$Title,
        [int]$Hue
    )
    $hue2 = ($Hue + 45) % 360
    $safeTitle = [System.Security.SecurityElement]::Escape($Title)
    return @"
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300" width="400" height="300" role="img" aria-label="$safeTitle">
  <defs>
    <linearGradient id="c" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="hsl($Hue, 62%, 58%)"/>
      <stop offset="100%" stop-color="hsl($hue2, 66%, 42%)"/>
    </linearGradient>
  </defs>
  <rect width="400" height="300" fill="url(#c)"/>
  <circle cx="70" cy="60" r="90" fill="rgba(255,255,255,0.12)"/>
  <circle cx="340" cy="250" r="110" fill="rgba(0,0,0,0.08)"/>
  <text x="200" y="166" text-anchor="middle" font-family="Segoe UI, Helvetica, Arial, sans-serif"
        font-size="30" font-weight="700" fill="#ffffff">$safeTitle</text>
</svg>
"@
}

$products = @(
    @{ file = 'aurora-headphones.svg';  title = 'Aurora Headphones'; sub = 'Hybrid ANC over-ear';      hue = 232; glyph = 'headphones' },
    @{ file = 'aurora-headphones-alt.svg'; title = 'Aurora Folded'; sub = 'Travel ready';           hue = 258; glyph = 'headphones' },
    @{ file = 'nimbus-speaker.svg';     title = 'Nimbus Speaker';  sub = '360 degree sound';         hue = 196; glyph = 'speaker' },
    @{ file = 'echo-buds.svg';          title = 'Echo Buds Pro';   sub = 'True wireless';            hue = 276; glyph = 'buds' },
    @{ file = 'pulse-watch.svg';        title = 'Pulse Watch';     sub = 'AMOLED fitness watch';      hue = 154; glyph = 'watch' },
    @{ file = 'pulse-watch-alt.svg';    title = 'Pulse Activity';  sub = 'GPS and recovery';          hue = 178; glyph = 'watch' },
    @{ file = 'vertex-keyboard.svg';    title = 'Vertex Keyboard'; sub = 'Hot-swappable 75%';        hue = 22;  glyph = 'keyboard' },
    @{ file = 'nova-stand.svg';         title = 'Nova Stand';      sub = 'Aluminium riser';           hue = 206; glyph = 'stand' },
    @{ file = 'orbit-backpack.svg';     title = 'Orbit Backpack';  sub = '22L commuter';              hue = 12;  glyph = 'backpack' },
    @{ file = 'lumen-monitor.svg';      title = 'Lumen 4K';        sub = '27 inch IPS panel';         hue = 250; glyph = 'monitor' },
    @{ file = 'halo-lamp.svg';          title = 'Halo Lamp';       sub = 'Dimmable LED task light';   hue = 44;  glyph = 'lamp' },
    @{ file = 'drift-chair.svg';        title = 'Drift Chair';     sub = 'Ergonomic mesh';           hue = 320; glyph = 'chair' },
    @{ file = 'terra-mat.svg';          title = 'Terra Mat';       sub = '6mm natural rubber';        hue = 140; glyph = 'mat' },
    @{ file = 'kinetic-dumbbells.svg';  title = 'Kinetic Dumbbells'; sub = 'Dial adjustable';         hue = 8;   glyph = 'dumbbell' },
    @{ file = 'zen-dinner-set.svg';     title = 'Zen Dinner Set';  sub = 'Stoneware for four';        hue = 186; glyph = 'plates' },
    @{ file = 'quantum-ssd.svg';        title = 'Quantum SSD';     sub = '1TB portable';              hue = 264; glyph = 'ssd' }
)

$categories = @(
    @{ file = 'audio.svg';        title = 'Audio';         hue = 232 },
    @{ file = 'wearables.svg';    title = 'Wearables';     hue = 154 },
    @{ file = 'accessories.svg';  title = 'Accessories';   hue = 22 },
    @{ file = 'displays.svg';     title = 'Displays';      hue = 250 },
    @{ file = 'home-office.svg';  title = 'Home & Office'; hue = 44 },
    @{ file = 'fitness.svg';      title = 'Fitness';       hue = 140 },
    @{ file = 'kitchen.svg';      title = 'Kitchen';       hue = 186 },
    @{ file = 'storage.svg';      title = 'Storage';       hue = 264 }
)

$productDir = Join-Path $WebappPath 'images\products'
$categoryDir = Join-Path $WebappPath 'images\categories'

foreach ($p in $products) {
    New-SvgFile -Path (Join-Path $productDir $p.file) -Content (New-ProductSvg -Title $p.title -Subtitle $p.sub -Hue $p.hue -Glyph $p.glyph)
}

foreach ($c in $categories) {
    New-SvgFile -Path (Join-Path $categoryDir $c.file) -Content (New-CategorySvg -Title $c.title -Hue $c.hue)
}

Write-Host 'Artwork generated.'
