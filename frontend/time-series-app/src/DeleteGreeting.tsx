import { useEffect, useState } from "react"
import axios from "axios"
import { Greeting } from "./Greeting";

const backendUrl = "http://localhost:8080/api";
axios.defaults.baseURL = backendUrl;

const getGreetings = () => axios.get<Greeting[]>("/greetings");
const deleteGreetings = (l: string) => axios.delete("/greetings", {params: {"language": l}});

export const DeleteGreeting = () => {

    const [selectedLanguage, setSelectedLanguage] = useState<string | undefined>();
    const [languages, setLanguages] = useState<string[]>([]);

    const fetchLanguages = async () => {
        try {
            const response = await getGreetings();
            const fetchedLanguages = response.data.map(g => g.language)

            setLanguages(fetchedLanguages)
            setSelectedLanguage(fetchedLanguages[0]);
        } catch (error) {
            console.log(error)
        }
    };

    const deleteGreeting = async (event: React.SyntheticEvent) => {
        try {
            event.preventDefault();
            const target = event.target as typeof event.target & {
                language: { value: string };
            };
            const l = target.language.value;
            await deleteGreetings(l);
        } catch (error) {
            console.error(error);
        }
    };

    useEffect(() => { fetchLanguages(); }, []);

    return (
        <form onSubmit={deleteGreeting}>
            <div><label>Remove greeting</label></div>
            <label>Greeting to remove:</label>
            <select
                name="language"
                value={selectedLanguage ?? ""}
                onChange={e => setSelectedLanguage(e.target.value ?? "")}
            >
                {Object.values(languages).map(l => <option value={l} key={l}>{l}</option>)}
            </select>
            <div>
                <input type="submit" value="Remove"></input>
            </div>
        </form>
    );
}