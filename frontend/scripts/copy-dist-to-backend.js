const fs = require('fs');
const path = require('path');

const frontendRoot = path.resolve(__dirname, '..');
const distPath = path.join(frontendRoot, 'dist', 'rubrica-java-angular', 'browser');
const backendStaticPath = path.resolve(frontendRoot, '..', 'backend', 'src', 'main', 'resources', 'static');

if (!fs.existsSync(distPath)) {
  throw new Error(`Build Angular non trovata: ${distPath}`);
}

fs.rmSync(backendStaticPath, { recursive: true, force: true });
fs.mkdirSync(backendStaticPath, { recursive: true });
fs.cpSync(distPath, backendStaticPath, { recursive: true });
console.log(`Build copiata in ${backendStaticPath}`);

