import { useEffect, useState } from "react"
import axios from "axios"
import { Greeting } from "./Greeting";

const backendUrl = "http://localhost:8080/api";
axios.defaults.baseURL = backendUrl;

const getGreetings = () => axios.get<Greeting[]>("/greetings");

export const GreetingView = () => {
    const [selectedGreeting, setSelectedGreeting] = useState<Greeting | undefined>();
    const [greetings, setGreetings] = useState<{ [key: string]: Greeting }>({});

    const fetchGreetings = async () => {
        try {
            const response = await getGreetings();
            const byLang = response.data.reduce((m, g) => ({ ...m, [g.language]: g}), {});
            setGreetings(byLang);
            setSelectedGreeting(response.data[0]);
        } catch (error) {
            console.error(error);
        }
    };

    useEffect(() => { fetchGreetings(); }, []);

    return (
        <>
            <div>
                <div><label>Select a languge to great:</label></div>
                <select
                    value={selectedGreeting?.language ?? ""}
                    onChange={e => setSelectedGreeting(greetings[e.target.value] ?? "error")}
                >
                    {Object.values(greetings).map(g =>
                        <option value={g.language} key={g.language}>{g.language}</option>
                        )
                    }
                </select>
                <h1>{selectedGreeting?.text}</h1>
            </div>
        </>
    );
};
