const fs=require('fs');const path=require('path');const {app}=require('electron');
function configPath(){return path.join(app.getPath('userData'),'config.json')}
function loadConfig(){try{return JSON.parse(fs.readFileSync(configPath(),'utf8'))}catch{return {mode:'demo'}}}
function saveConfig(patch){const current=loadConfig();const next={...current,...patch};fs.mkdirSync(path.dirname(configPath()),{recursive:true});fs.writeFileSync(configPath(),JSON.stringify(next,null,2),{mode:0o600});return next}
module.exports={loadConfig,saveConfig};
