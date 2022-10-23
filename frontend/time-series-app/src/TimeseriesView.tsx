import { useEffect, useState } from "react"
import axios from "axios"
import { Timeseries } from "./Timeseries";

const backendUrl = "http://localhost:8080/timeseries";
axios.defaults.baseURL = backendUrl;

const getTimeseries = () => axios.get<Timeseries[]>("/powerstations");
const getTimeseriesByPowerstation = (powerstationName: string) => axios.get<Timeseries[]>("/datesbypowerstation?powerstation="+powerstationName);
const getTimeseriesByPowerstationAndDate = (powerstationName: string, date: string) => axios.get<Timeseries[]>("/seriesbypowerstationanddate?powerstation="+powerstationName+"&date="+date);

export const TimeseriesView = () => {
    const [selectedTimeseriesItem, setSelectedTimeseriesItem] = useState<Timeseries | undefined>();
    const [timeseries, setTimeseries] = useState<{ [key: string]: Timeseries }>({});

    const fetchTimeseries = async () => {
        try {
            const response = await getTimeseries();
            const byPowerstation = response.data.reduce((m, g) => ({ ...m, [g.powerstation]: g}), {});
            setTimeseries(byPowerstation);
            setSelectedTimeseriesItem(response.data[0]);
            //alert("fetchTimeseries");
        } catch (error) {
            console.error(error);
        }
    };

    useEffect(() => { fetchTimeseries(); }, []);

    const [selectedItemByPowerstation, setSelectedItemByPowerstation] = useState<Timeseries | undefined>();
    const [timeseriesByPowerstation, setTimeseriesByPowerstation] = useState<{ [key: string]: Timeseries }>({});

    const fetchTimeseriesByPowerstation = async () => {
        try {
			//alert("fetchTimeseriesByPowerstation");
            const response = await getTimeseriesByPowerstation(selectedTimeseriesItem?.powerstation!);
            const byDate = response.data.reduce((m, g) => ({ ...m, [g.date]: g}), {});
            setTimeseriesByPowerstation(byDate);
            setSelectedItemByPowerstation(response.data[0]);
        } catch (error) {
            console.error(error);
        }
    };

	const powerstationChange = async (event: any) => {
		//alert("usePowerstationChange");
		setSelectedTimeseriesItem(timeseries[event.target.value] ?? "error")
	    fetchTimeseriesByPowerstation();
	}

    const [selectedItemByPowerstationDate, setSelectedItemByPowerstationDate] = useState<Timeseries | undefined>();
    const [timeseriesByPowerstationDate, setTimeseriesByPowerstationDate] = useState<{ [key: string]: Timeseries }>({});

    const fetchTimeseriesByPowerstationAndDate = async () => {
        try {
            const response = await getTimeseriesByPowerstationAndDate(selectedTimeseriesItem?.powerstation!, selectedTimeseriesItem?.date!);
            const bySeries = response.data.reduce((m, g) => ({ ...m, [g.series]: g}), {});
            setTimeseriesByPowerstationDate(bySeries);
            setSelectedItemByPowerstationDate(response.data[0]);
        } catch (error) {
            console.error(error);
        }
    };

	const dateChange = async (event: any) => {
		alert("dateChange");
		//setSelectedTimeseriesItem(timeseries[event.target.value] ?? "error")
	    fetchTimeseriesByPowerstationAndDate();
	}

    //useEffect(() => { fetchTimeseriesByPowerstation(); }, []);
    useEffect(() => { fetchTimeseriesByPowerstationAndDate(); }, []);

    return (
        <>
			<h3>Query</h3>
            <div>
                <div><label>Select a powerstation:</label></div>
                <select
                    value={selectedTimeseriesItem?.powerstation ?? ""}
                    onChange={e => {
						//alert("onChange");
						setSelectedTimeseriesItem(timeseries[e.target.value] ?? "error")
					    //fetchTimeseriesByPowerstation();
					    powerstationChange(e)
					}}
                >
                    {Object.values(timeseries).map(g =>
                        <option value={g.powerstation} key={g.powerstation}>{g.powerstation}</option>
                        )
                    }
                </select>
                <h4>{selectedTimeseriesItem?.powerstation}</h4>
                <p />

                <div><label>Select a Day:</label></div>
                <select
                    value={selectedItemByPowerstation?.date ?? ""}
                    onChange={e => {
						setSelectedItemByPowerstation(timeseriesByPowerstation[e.target.value] ?? "error")
						dateChange(e)
						}}
                >
                    {Object.values(timeseriesByPowerstation).map(g =>
                        <option value={g.date} key={g.date}>{g.date}</option>
                        )
                    }
                </select>
                <h4>{selectedItemByPowerstation?.date}</h4>
                
                {timeseriesByPowerstationDate.series}
                
            </div>
        </>
    );
};
