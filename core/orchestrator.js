const { randomUUID } = require('crypto');
async function runProvisioning(input, adapters={}) {
  const customer=input.customer||{}; if(!customer.name||!customer.phone) throw new Error('Nome e WhatsApp do cliente são obrigatórios.');
  const id=randomUUID();
  if(adapters.demo){const test={id:'TEST-'+id.slice(0,8).toUpperCase(),status:'created',expiresInHours:2};const m3u='https://example.invalid/m3u/'+id;return {id,mode:'demo',customer,steps:[{step:'masterflix.createTest',status:'ok',test},{step:'masterflix.activateMecAndGetM3u',status:'ok'},{step:'xcloud.activateMecAndUploadM3u',status:'ok'},{step:'gerenciaApp.activateMecAndUploadM3u',status:'ok'},{step:'whatsapp.buildSuccessMessage',status:'ok'}],result:{test,m3u,message:'Fluxo de demonstração concluído.'}};}
  const {masterflix,xcloud,gerenciaApp,whatsapp}=adapters; const test=await masterflix.createTest(input); const m3u=await masterflix.activateMecAndGetM3u(test); await xcloud.activateMecAndUploadM3u({customer,m3u}); await gerenciaApp.activateMecAndUploadM3u({customer,m3u}); return whatsapp.buildSuccessMessage({customer,test,m3u});
}
module.exports={runProvisioning};
