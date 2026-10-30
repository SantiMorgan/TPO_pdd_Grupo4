async function get(path, signal) {
  const response = await fetch(`/api${path}`, { signal });
  if (!response.ok) throw new Error(`La API respondió HTTP ${response.status}`);
  return response.json();
}

export async function consultarEstado(signal) {
  const [aplicacion, salud] = await Promise.all([get('', signal), get('/health', signal)]);
  return { ...aplicacion, salud: salud.status };
}
