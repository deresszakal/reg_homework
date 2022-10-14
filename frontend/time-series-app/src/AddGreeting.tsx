import axios from "axios"

const backendUrl = "http://localhost:8080/api";
axios.defaults.baseURL = backendUrl;

const putGreeting = (l: string, t: string) => axios.put("/greetings?language=" + l + "&text=" + t);


export const AddGreeting = () => {

    const saveGreeting = async (event: React.SyntheticEvent) => {
        try {
            event.preventDefault();
            const target = event.target as typeof event.target & {
                language: { value: string };
                text: { value: string };
            };
            const language = target.language.value;
            const text = target.text.value;
            await putGreeting(language, text);
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <form onSubmit={saveGreeting}>
            <div><label>Add new greeting</label></div>
            <div>
                <label>Language:
                    <input type="text" name="language"/>
                </label>
            </div>
            <div>
                <label>Greeting text:
                    <input type="text" name="text"/>
                </label>
            </div>
            <div>
                <input type="submit" value="Add"/>
            </div>
        </form>
    );
};
