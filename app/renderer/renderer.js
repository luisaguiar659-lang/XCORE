const $ = (id) => document.getElementById(id);
const titles = { dashboard:['Dashboard','Central de automação do XCORE.'], integrations:['Integrações','Status dos conectores externos.'], automation:['Automação','Fluxo de provisionamento do cliente.'], settings:['Configurações','Preferências locais do aplicativo.'] };
async function refresh() {
  const config = await window.xcore.getConfig(); $('mode').value = config.mode || 'demo';
  const configured = { whatsapp:Boolean(config.whatsappAccessToken), masterflix:Boolean(config.masterflixBaseUrl), xcloud:Boolean(config.xcloudBaseUrl), gerencia:Boolean(config.gerenciaAppBaseUrl) };
  for (const [key,value] of Object.entries(configured)) {
    const el=$(`${key}-status`); if(el) el.textContent=value?'Configurado':'Não configurado';
    const map={gerencia:'ga',whatsapp:'wa',masterflix:'mf',xcloud:'xc'}; const intEl=$(`int-${map[key]}`); if(intEl) intEl.textContent=value?'Configurado':'Pendente';
  }
}
document.querySelectorAll('.nav').forEach(btn=>btn.addEventListener('click',()=>{document.querySelectorAll('.nav').forEach(x=>x.classList.remove('active'));document.querySelectorAll('.view').forEach(x=>x.classList.remove('active'));btn.classList.add('active');const view=btn.dataset.view;$(view).classList.add('active');$('page-title').textContent=titles[view][0];$('page-subtitle').textContent=titles[view][1];}));
$('run-demo').addEventListener('click',async()=>{ $('output').textContent='Executando…'; try{$('output').textContent=JSON.stringify(await window.xcore.runDemo({customer:{name:$('demo-name').value.trim(),phone:$('demo-phone').value.trim()}}),null,2)}catch(e){$('output').textContent=e.message||String(e)} });
$('save').addEventListener('click',async()=>{await window.xcore.saveConfig({mode:$('mode').value});$('save-msg').textContent='Configuração salva.';setTimeout(()=>$('save-msg').textContent='',2500)}); refresh();
