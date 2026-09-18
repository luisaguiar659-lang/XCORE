/**
 * Main provisioning workflow.
 *
 * Integration adapters are intentionally isolated so credentials,
 * authentication and panel-specific APIs do not leak into the workflow.
 */
async function provisionCustomer(input, adapters) {
  const {
    whatsapp,
    masterflix,
    xcloud,
    gerenciaApp,
  } = adapters;

  const test = await masterflix.createTest(input);
  const m3u = await masterflix.activateMecAndGetM3u(test);

  await xcloud.activateMecAndUploadM3u({
    customer: input.customer,
    m3u,
  });

  await gerenciaApp.activateMecAndUploadM3u({
    customer: input.customer,
    m3u,
  });

  return whatsapp.buildSuccessMessage({
    customer: input.customer,
    test,
    m3u,
  });
}

module.exports = { provisionCustomer };
