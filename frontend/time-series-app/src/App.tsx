import './app.css';
import { useNavigate } from "react-router-dom";

export const App = () => {
	
	let navigate = useNavigate(); 
	const routeToGreting = () =>{
        clearElemets('mainPanel');
        navigate("/GreetingPage", { replace: true });
	}
	const routeToTimeseries = () =>{ 
		clearElemets('mainPanel');
		navigate("/TimeseriesPage", {replace: true});
	}
	const goBack = () => {
		clearElemets('mainPanel');
    	navigate(-1)
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
		<div id="mainPanel" className="base_panel">
			<h1>Homework</h1>
			<button
				onClick={routeToGreting}
				className='panel_button' 
				title="Greetings"
			>
			Greetings
			</button>
			<p/>
			<button
				onClick={routeToTimeseries}
				className='panel_button' 
				title="Time-series"
			>
			Timeseries
			</button>
		</div>
	);
};