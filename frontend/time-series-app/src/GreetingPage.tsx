import { AddGreeting } from './AddGreeting';
import { GreetingView } from './GreetingView';
import { DeleteGreeting } from './DeleteGreeting';
import { useNavigate } from 'react-router-dom';

export const GreetingPage = () => {
	
	let navigate = useNavigate(); 
	const goBack = () => {
		clearElemets('greetingPanel');
    	navigate('/Home', {replace: true})
    }

	let clearElemets = (nodeID: string) => {
        var panel = document.getElementById(nodeID);
		var child = panel?.lastElementChild; 
        	while (child) {
            panel?.removeChild(child);
            child = panel?.lastElementChild;
        }
	}


  return (
    <div id='greetingPanel' className='base_panel' >
      <GreetingView />
      <hr/>
      <AddGreeting />
      <hr/>
      <DeleteGreeting />
      <hr/>
		<button
			onClick={goBack}
			className='panel_button' 
			title='Greetings'
		>
		Go Home
		</button>
    </div>
  );
};

