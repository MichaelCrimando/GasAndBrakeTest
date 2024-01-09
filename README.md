Used to prove out virtual input device and InputManager work.

**Instructions:**
1. This uses reflection to access hidden InputManager methods so it needs to be a system-level app. Build with an OEM signing key, install on a userdebug software.
2. Install a gamepad tester. Could be a game, but I like to use Gamepad Tester. https://play.google.com/store/apps/details?id=ru.elron.gamepadtester&hl=en_US&gl=US
3. Launch GasAndBrakeTest
4. Tap the button to start the service which sends gas, brake and x-axis commands once a second.
5. Launch your test app and verify inputs are being sent to the app.
