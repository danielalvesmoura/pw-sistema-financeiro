import "./Input.css";


export default function Input({
    label,
    type = "text",
    value,
    onChange,
    placeholder,
    name,
    icon
}) {
    return (
        <div className="input-container">
            <label className="input-label">{label}</label>

            <div className="wrapper">
                <span className="icon">{icon}</span>
                <input
                    className={`input-field`}
                    type={type}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    name={name}
                />
            </div>
            
        </div>
    );
}