import type { FormEvent } from "react";

interface PromptFormProps {
  prompt: string;
  isSubmitting: boolean;
  onPromptChange: (prompt: string) => void;
  onSubmit: () => void;
}

const MAX_PROMPT_LENGTH = 500;

function PromptForm({
  prompt,
  isSubmitting,
  onPromptChange,
  onSubmit,
}: PromptFormProps) {
  const trimmedPrompt = prompt.trim();
  const isTooLong = prompt.length > MAX_PROMPT_LENGTH;
  const isDisabled = !trimmedPrompt || isTooLong || isSubmitting;

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!isDisabled) {
      onSubmit();
    }
  };

  return (
    <section className="panel prompt-panel">
      <h2>1. 프롬프트 입력</h2>
      <p className="section-help">
        애니메이션으로 만들고 싶은 내용을 입력해주세요.
      </p>

      <form onSubmit={handleSubmit}>
        <label className="sr-only" htmlFor="prompt">
          프롬프트
        </label>
        <div className={`textarea-wrap ${isTooLong ? "is-error" : ""}`}>
          <textarea
            id="prompt"
            maxLength={MAX_PROMPT_LENGTH + 100}
            onChange={(event) => onPromptChange(event.target.value)}
            placeholder="예: 숲 속에서 곰이 꿀을 발견하고 맛있게 먹는 30초 애니메이션을 만들어줘."
            value={prompt}
          />
          <span className="character-count">
            {prompt.length} / {MAX_PROMPT_LENGTH}
          </span>
        </div>

        {isTooLong && (
          <p className="field-error">프롬프트는 500자 이하로 입력해주세요.</p>
        )}

        <button className="primary-button" disabled={isDisabled} type="submit">
          <span aria-hidden="true">✦</span>
          {isSubmitting ? "생성 중" : "생성하기"}
        </button>
      </form>

      <div className="info-box">
        <span className="info-icon" aria-hidden="true">
          i
        </span>
        <span className="info-text">
          입력된 프롬프트는 GPT-5.4 mini를 통해 Scene으로 변환되며, Nano
          Banana와 Kling-2.6을 사용하여 애니메이션이 생성됩니다.
        </span>
      </div>
    </section>
  );
}

export default PromptForm;
