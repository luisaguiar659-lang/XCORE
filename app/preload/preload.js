const { contextBridge, ipcRenderer } = require('electron');
contextBridge.exposeInMainWorld('xcore', {
  getConfig: () => ipcRenderer.invoke('xcore:get-config'),
  saveConfig: (config) => ipcRenderer.invoke('xcore:save-config', config),
  runDemo: (input) => ipcRenderer.invoke('xcore:run-demo', input)
});
