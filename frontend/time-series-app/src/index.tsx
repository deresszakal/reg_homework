import ReactDOM from 'react-dom';
import './index.css';
import { App } from './App';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { GreetingPage } from "./GreetingPage";
import { TimeseriesPage } from "./TimeseriesPage";

ReactDOM.render(
	<BrowserRouter>
	  <Routes>
          <Route path="/GreetingPage" element={<GreetingPage />} />
          <Route path="/TimeseriesPage" element={<TimeseriesPage />} />
          <Route path="/Home" element={<App />} />
		</Routes>

    	<App />
    </BrowserRouter>,
  document.getElementById('root')
);
