# BL831 Crystal Centering UI

Touchscreen interface for video-based crystal centering at the BL831 beamline (Advanced Light Source, Lawrence Berkeley National Laboratory). Displays a live camera feed with beam overlay and provides controls for goniometer rotation, centering, zoom, pin selection, and light adjustment.

Video is captured via FFmpeg (Axis IP cameras or V4L2 devices). Button presses are translated to DCSS commands through a touch server.

## Install

Download the latest release zip from the [releases page](https://github.com/bl831/touchscreen/releases) and unzip:

```bash
unzip crystmntui-*-distribution.zip -d /opt/crystmntui
```

This gives you:

```
/opt/crystmntui/
  bin/crystmntui        # launcher script
  lib/crystmntui-*-bundled.jar
  examples/             # theme configs and assets
```

Add `bin/` to your PATH or run directly: `/opt/crystmntui/bin/crystmntui`

Requires JRE 11+.

## Build from Source

Requires JDK 11+ and Maven 3.6+.

```bash
mvn package
```

The bundled jar is written to `target/`.

## Usage

```bash
crystmntui \
  -v axis://host/axis-cgi/mjpg/video.cgi?camera=1 \
  -t touch://hostname:14000
```

Or launch directly with Java:

```bash
java -jar crystmntui-*-bundled.jar \
  -v axis://host/axis-cgi/mjpg/video.cgi?camera=1 \
  -t touch://hostname:14000
```

### Options

| Flag | Description |
|------|-------------|
| `-v, --video` | Video source URI (`axis://` or `v4l2://`) |
| `-t, --touch` | Touch server URI (`touch://hostname:port`) |
| `-c, --config` | Path to config file |
| `-w, --window` | Window size (`1920x1080`) or `full`. Default: `full` (undecorated kiosk mode) |
| `-i, --interpolation` | Image scaling: `nearest`, `bilinear`, or `bicubic` |
| `-e, --emulate` | Emulate old-style touch coordinates |
| `-l, --list-v4l2` | List available V4L2 video capture URIs |

### V4L2 example

```bash
crystmntui -v v4l2://video0/MJPG/640x480/30fps -t touch://localhost:14000
```

## Config Files

A `.config` file is a Java properties file that controls theming and button overrides. Pass it with `-c`.

### CLI defaults

Config files can set defaults for CLI options using `cli.<long-flag-name>`. Command line flags override these.

```properties
cli.video=axis://host/axis-cgi/mjpg/video.cgi?camera=1
cli.touch=touch://hostname:14000
cli.window=1920x1080
cli.emulate=false
cli.interpolation=bilinear
```

### UI properties

```properties
ui.background=#000810
ui.border-color=#00e5ff
ui.borderless-buttons=true
```

### Button overrides

Each button can have a custom icon, pressed icon, command, or be hidden:

```properties
button.ZoomIn.icon=ZoomIn.png
button.ZoomIn.pressed=ZoomIn-pressed.png
button.ZoomIn.command=some_dcss_command
button.ZoomIn.hidden=true
```

Icon paths are resolved relative to the config file's directory. Available button names: `ZoomIn`, `ZoomOut`, `Plus10`, `Plus90`, `HalfTurn`, `Minus90`, `Minus10`, `Undo`, `Park`, `UnPark`, `Polarizer`, `Light`, `Pin24mm`..`Pin10mm`, `MoveUp`, `MoveDown`, `MoveLeft`, `MoveRight`, `MoveIn`, `MoveOut`, `Center`.

The `Light` button acts as a vertical slider. The `Center` button maps to clicks on the video area.

## Themes

Two example themes are included under `examples/`:

- **dark-neon** — Cyan neon glow on dark background
- **arc-reactor** — Brushed metal with stamped beveled buttons

Each theme directory contains a config file, button PNGs (normal and `-pressed` variants), and the HTML design file used to generate the assets.

```bash
crystmntui \
  -v axis://host/axis-cgi/mjpg/video.cgi?camera=1 \
  -t touch://hostname:14000 \
  -c examples/arc-reactor/arc-reactor.config
```

## Hardware

Linux only. V4L2 device access requires the `camerainfo` library (included).

- **Axis IP cameras** — MJPEG streams via `axis://host/path?camera=N`
- **V4L2 devices** — Local capture via `v4l2://deviceN/FORMAT/WxH/FPS`. Use `-l` to list available devices. Tested with [USB HDMI capture cards](https://www.amazon.com/dp/B0FR7QD83J).
- **Touchscreen** — Any display; touch events are translated to DCSS commands via the touch server
