import BartenderView from '../views/bartender/BartenderView.jsx';
import EncargadoView from '../views/encargado/EncargadoView.jsx';

export default function VistaPorRol({ rol }) {
  return rol === 'encargado' ? <EncargadoView /> : <BartenderView />;
}
