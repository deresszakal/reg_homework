import { Component } from "react";
import axios from "axios";

// forrás: https://kris101.medium.com/react-file-upload-the-easy-way-with-nodejs-e94c5e81fb8

// SET your own endpoint
const endpoint = "http://localhost:8080/timeseries/uploadmultipart";

export class TimeseriesUpload extends Component {
	state = {
		selectedFile: null,
		loaded: 0,
		message: "Choose a file...",
		defaultmessage: "Choose a file...",
		uploading: false
	};
	handleFileChange = (event: any) => {
		this.setState({
			selectedFile: event.target.files[0],
			loaded: 0,
			message: event.target.files[0]
				? event.target.files[0].name
				: this.state.defaultmessage
		});
	};
	handleUpload = (event: any) => {
		event.preventDefault();
		if (this.state.uploading) return;
		if (!this.state.selectedFile) {
			this.setState({ message: "Select a file first" });
			return;
		}
		this.setState({ uploading: true });
		// define upload
		const data = new FormData();
		let selectedFile = this.state.selectedFile;
		if (selectedFile != null) {
			data.append("file", selectedFile);
			axios
				.put(endpoint, data, {
					onUploadProgress: ProgressEvent => {
						this.setState({
							loaded: Math.round(
								(ProgressEvent.loaded / ProgressEvent.total) * 100
							)
						});
					}
				})
				.then(res => {
					this.setState({
						selectedFile: null,
						message: "Uploaded successfully",
						uploading: false
					});
				})
				.catch(err => {
					this.setState({
						uploading: false,
						message: ("Failed to upload: " + err.response.data)
					});
				});
		}
	};
	render() {
		return (
			<form className="box" onSubmit={this.handleUpload}>
				<h3>Upload</h3>
				<input
					type="file"
					name="file-5[]"
					id="file-5"
					className="inputfile inputfile-4"
					onChange={this.handleFileChange}
				/>
				<label htmlFor="file-5">
					<p />
					<span>
						{this.state.uploading
							? this.state.loaded + "%"
							: this.state.message}
					</span>
				</label>
				<span className="spacer_25" />
				<button className="submit" onClick={this.handleUpload}>
					Upload
				</button>
			</form>
		);
	}
}

