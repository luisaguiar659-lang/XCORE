const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const { runProvisioning } = require('../../core/orchestrator');
const { loadConfig, saveConfig } = require('../../core/config');
let mainWindow;
function createWindow() {
  mainWindow = new BrowserWindow({ width:1180, height:760, minWidth:980, minHeight:640, backgroundColor:'#0b1020',
    webPreferences:{ preload:path.join(__dirname,'../preload/preload.js'), contextIsolation:true, nodeIntegration:false }});
  mainWindow.loadFile(path.join(__dirname,'../renderer/index.html'));
}
app.whenReady().then(() => {
  createWindow();
  ipcMain.handle('xcore:get-config', () => loadConfig());
  ipcMain.handle('xcore:save-config', (_event, config) => saveConfig(config));
  ipcMain.handle('xcore:run-demo', async (_event, input) => runProvisioning(input, { demo:true }));
  app.on('activate', () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });
});
app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });
