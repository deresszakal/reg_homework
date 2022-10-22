import { useNavigate } from "react-router-dom";

export const TimeseriesPage = () => {

	let navigate = useNavigate(); 
	const goBack = () => {
		clearElemets('timeseriesPanel');
    	navigate("/Home", {replace: true})
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
	<div id='timeseriesPanel' className='base_panel'>
		<h1>Timeseries Page</h1>  

      <hr/>
		<button
			onClick={goBack}
			className='panel_button' 
			title="Greetings"
		>
		Go Home
		</button>
    </div>
  );
};

